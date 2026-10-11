#!/usr/bin/env bash
# Installs verified tools outside the checkout, preserving portable build files.
set -euo pipefail
project_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
source "$project_root/scripts/cloud-env.sh"
mkdir -p "$DINO_TOOLING_ROOT/downloads" "$ANDROID_HOME" "$ANDROID_USER_HOME" "$GRADLE_USER_HOME"

if [[ ! -x "$JAVA_HOME/bin/javac" ]]; then
  jdk_url='https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.12.1%2B1/OpenJDK21U-jdk_x64_linux_hotspot_21.0.12.1_1.tar.gz'
  curl -fsSL --retry 2 "$jdk_url" -o "$DINO_TOOLING_ROOT/downloads/jdk21-current.tar.gz"
  curl -fsSL --retry 2 "$jdk_url.sha256.txt" -o "$DINO_TOOLING_ROOT/downloads/jdk21-current.tar.gz.sha256.txt"
  python3 - <<'PY'
import hashlib, os, pathlib, tarfile
root = pathlib.Path(os.environ['DINO_TOOLING_ROOT'])
p = root / 'downloads/jdk21-current.tar.gz'
expected = pathlib.Path(str(p) + '.sha256.txt').read_text().split()[0]
assert hashlib.sha256(p.read_bytes()).hexdigest() == expected, 'JDK checksum mismatch'
tarfile.open(p).extractall(root, filter='data')
PY
fi

# Java does not automatically use HTTPS_PROXY or the cloud's custom CA bundle.
# Add the supplied CAs to the standard JDK trust store; do not disable TLS checks.
python3 - <<'PY'
import hashlib, os, pathlib, shutil, subprocess, urllib.parse
root = pathlib.Path(os.environ['DINO_TOOLING_ROOT'])
base = pathlib.Path(os.environ['JAVA_HOME']) / 'lib/security/cacerts'
ca = pathlib.Path(os.environ['SSL_CERT_FILE']) if os.environ.get('SSL_CERT_FILE') else None
ca_bytes = ca.read_bytes() if ca else b''
signature = hashlib.sha256(base.read_bytes() + ca_bytes).hexdigest()
trust = root / 'java-cacerts'
stamp = root / 'java-cacerts.sha256'
if not trust.exists() or not stamp.exists() or stamp.read_text() != signature:
    shutil.copyfile(base, trust)
    for i, part in enumerate(ca_bytes.decode().split('-----END CERTIFICATE-----')):
        if '-----BEGIN CERTIFICATE-----' not in part:
            continue
        cert = root / f'ca-{i}.pem'
        cert.write_text(part + '-----END CERTIFICATE-----\n')
        try:
            subprocess.run([str(pathlib.Path(os.environ['JAVA_HOME']) / 'bin/keytool'), '-importcert',
                '-noprompt', '-alias', f'cloud-ca-{i}', '-file', str(cert), '-keystore', str(trust),
                '-storepass', 'changeit'], check=True, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        finally:
            cert.unlink()
    stamp.write_text(signature)
props = [f'systemProp.javax.net.ssl.trustStore={trust}', 'systemProp.http.nonProxyHosts=localhost|127.*']
for scheme in ('http', 'https'):
    value = os.environ.get(scheme.upper() + '_PROXY')
    if value:
        proxy = urllib.parse.urlsplit(value)
        if proxy.username or proxy.password:
            raise RuntimeError('Proxy with credentials requires supported external authentication; do not save credentials')
        props += [f'systemProp.{scheme}.proxyHost={proxy.hostname}', f'systemProp.{scheme}.proxyPort={proxy.port or 80}']
pathlib.Path(os.environ['GRADLE_USER_HOME'], 'gradle.properties').write_text('\n'.join(props) + '\n')
PY

if [[ ! -x "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" ]]; then
  curl -fsSL --retry 2 https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip -o "$DINO_TOOLING_ROOT/downloads/commandlinetools.zip"
  curl -fsSL --retry 2 https://dl.google.com/android/repository/repository2-1.xml -o "$DINO_TOOLING_ROOT/downloads/repository.xml"
  python3 - <<'PY'
import hashlib, os, pathlib, xml.etree.ElementTree as ET, zipfile
root = pathlib.Path(os.environ['DINO_TOOLING_ROOT'])
p = root / 'downloads/commandlinetools.zip'
archives = ET.parse(root / 'downloads/repository.xml').getroot().findall('./remotePackage/archives/archive')
match = next(a for a in archives if a.findtext('./complete/url') == 'commandlinetools-linux-13114758_latest.zip')
assert hashlib.sha1(p.read_bytes()).hexdigest() == match.findtext('./complete/checksum'), 'SDK tools checksum mismatch'
dest = pathlib.Path(os.environ['ANDROID_HOME']) / 'cmdline-tools'
zipfile.ZipFile(p).extractall(dest)
(dest / 'cmdline-tools').rename(dest / 'latest')
for tool in (dest / 'latest/bin').iterdir():
    tool.chmod(0o755)
PY
fi

# Use JVM properties for both HTTPS transport and the verified trust store.
sdk_java_options="$(python3 - <<'PY'
import os, pathlib, urllib.parse
options = [f"-Djavax.net.ssl.trustStore={os.environ['DINO_TOOLING_ROOT']}/java-cacerts"]
for scheme in ('http', 'https'):
    value = os.environ.get(scheme.upper() + '_PROXY')
    if value:
        p = urllib.parse.urlsplit(value)
        options += [f'-D{scheme}.proxyHost={p.hostname}', f'-D{scheme}.proxyPort={p.port or 80}']
print(' '.join(options))
PY
)"
if [[ ! -f "$ANDROID_HOME/platforms/android-36/android.jar" || ! -x "$ANDROID_HOME/build-tools/36.0.0/aapt2" || ! -x "$ANDROID_HOME/platform-tools/adb" ]]; then
  # A finite input stream avoids hiding the sdkmanager exit code behind a pipeline.
  python3 -c "print('y\\n' * 100)" > "$DINO_TOOLING_ROOT/sdk-license-input"
  JAVA_TOOL_OPTIONS="$sdk_java_options" sdkmanager --sdk_root="$ANDROID_HOME" --licenses < "$DINO_TOOLING_ROOT/sdk-license-input"
  rm "$DINO_TOOLING_ROOT/sdk-license-input"
  JAVA_TOOL_OPTIONS="$sdk_java_options" sdkmanager --sdk_root="$ANDROID_HOME" 'platforms;android-36' 'build-tools;36.0.0' 'platform-tools'
fi
# ANDROID_HOME supplies the SDK path; no tracked/local configuration is overwritten.
printf 'Cloud tools ready. Source scripts/cloud-env.sh before building.\n'
