# Dino Expert debug APKs

| Version | Download | Content |
| --- | --- | --- |
| **0.3.0 — latest** | [APK](dino-expert-0.3.0-debug.apk) · [SHA-256](dino-expert-0.3.0-debug.apk.sha256) | Five levels, fifty questions, badges/final achievement, polished graphics, 360 offline Professor Fossil voice clips and chimes |
| 0.1.0 | [APK](dino-expert-0.1.0-debug.apk) · [SHA-256](dino-expert-0.1.0-debug.apk.sha256) | Original ten-question Phase 1 MVP |

Android 8.0 (API 26) or newer is required. The latest version targets API 36. These are debug-signed testing builds; production release signing belongs to Phase 4.

Download with GitHub’s download button, transfer the APK to your Android device, and open it. Android may ask you to allow installation from your chosen browser/file manager. Alternatively:

```bash
adb install -r releases/dino-expert-0.3.0-debug.apk
adb shell am start -n com.dinoexpert/.MainActivity
```

The old and new APK signing certificates match. Install the latest version over the MVP to keep progress; no uninstall is required. Save-schema migration is covered by JVM tests. Actual device update/restore validation remains on the QA checklist.

Verify the downloaded file:

```bash
cd releases
sha256sum -c dino-expert-0.3.0-debug.apk.sha256
```

The latest APK’s signature, exact fifty-question bank, all 360 voice files, and absence of Internet permission were checked before committing it. Twenty-five JVM tests passed; all speech and effect recordings decoded without silence/clipping. Android test APK compilation and lint passed. Seven Android tests remain unrun due to the cloud adb/emulator limitation. See [development status](../DEVELOPMENT_STATUS.md), [audio credits](../docs/ASSET_LICENSES.md) and the [device checklist](../docs/QA_CHECKLIST.md).
