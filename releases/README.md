# Phase 1 debug APK

`dino-expert-0.1.0-debug.apk` is the built, debug-signed Phase 1 MVP for Android 8.0 (API 26) and newer. It targets API 36 and contains the ten-question English bank. This is a testing build; production release signing belongs to Phase 4.

Download the APK using GitHub's download button, transfer it to your Android device, and open it. Android may ask you to allow app installation from the browser or file manager you use. Alternatively, install on a connected test device:

```bash
adb install -r releases/dino-expert-0.1.0-debug.apk
adb shell am start -n com.dinoexpert/.MainActivity
```

Verify the download on Linux:

```bash
cd releases
sha256sum -c dino-expert-0.1.0-debug.apk.sha256
```

The APK signature, exact bundled question asset, and absence of Internet permission were checked before committing it. Fourteen JVM tests passed; Android test APK compilation and lint passed. Device tests remain unrun because of the cloud machine's adb/emulator limitations. See [development status](../DEVELOPMENT_STATUS.md) and the [device checklist](../docs/QA_CHECKLIST.md).
