# Development status

## Phase 0 — complete

Inspected the empty checkout and tool availability. See [technical plan](docs/IMPLEMENTATION_PLAN.md) for the stack, package structure, state model, navigation, and milestone checklist.

## Phase 1 — implemented and build-validated; device QA pending

- [x] Android project with Gradle Kotlin DSL, Android 26–36 support.
- [x] Pure Kotlin quiz engine, serial MVVM actions and DataStore persistence.
- [x] Ten curated beginner questions in bundled JSON with explanations, hints and attribution.
- [x] Menu, play/continue, retries, stable shuffled A–D answers, correct feedback and explicit Next.
- [x] Completion badge and replay of Level 1.
- [x] Professor Fossil original Canvas illustration and five expression states.
- [x] Scrollable phone layout and width-based two-column tablet foundation.
- [x] System insets, scalable type, text/icon answer status, 48dp+ controls, sound and reduced-motion settings.
- [x] JVM rule/content/save tests and Android Compose/DataStore tests written.
- [x] Debug APK, Android test APK, JVM results and lint results verified.
- [ ] Device/emulator execution and manual accessibility/visual QA (environment limitation).

## Scope boundaries

Only Phase 1 is authorized in this execution. Levels 2–5, full level selection/unlocking UI, the 50-question bank, and final achievement are not implemented. The engine is prepared for multiple levels; MVP only exposes the one available level.

## Remaining phases — not started

2. Expand and editorially review all fifty questions; add level selection, progression, final achievement and comprehensive save tests.
3. Refine art, animations, prehistoric scenery, bundled sound assets and accessibility/device layouts.
4. Full release QA on physical phones and tablets, rotation, font scaling, TalkBack, offline restart and release packaging.

## Validation results — 2026-10-11

Command (after `source scripts/cloud-env.sh`):

```bash
./gradlew :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-daemon
```

- **Passed:** 14 JVM tests; zero failures, errors or skips. They use the actual bundled JSON and exercise retries, stable shuffling, duplicate actions, explicit Next, ten-question completion, engine-level locks/unlocks, replay, saves and invalid data.
- **Passed:** debug APK build (`app/build/outputs/apk/debug/app-debug.apk`) and Android test APK build. APK signature verified with `apksigner`.
- **Passed:** packaged question asset equals the source bank; APK manifest confirms minimum API 26, target API 36 and no Internet permission.
- **Passed:** Android lint, zero errors and one `NewerVersionAvailable` advisory (coroutines 1.11.0 exists; retain the tested stable 1.10.2 pin for this milestone).
- **Passed:** cloud setup helper executed and repeated successfully; official Gradle distribution and wrapper JAR, JDK archive, and SDK tools checksum verification retained. Java TLS trust uses the supplied cloud CA bundle plus the standard JDK trust store.
- **Built but unrun:** four Android tests for Compose interaction/layout, DataStore, activity recreation and relaunch. Current machine has no KVM; adb cannot create its user key/cache directory because the actual home filesystem is read-only, even after a scoped permission grant. Device UI, rotation, font scaling and TalkBack results are not claimed.
- **Unverified externally:** live museum source pages; retrieval returned proxy CONNECT 403. Established facts were reviewed locally; final editorial reference verification remains. See `docs/CONTENT_REVIEW.md`.

All project files are newly created in the original checkout; no existing application was overwritten. The initial project commit includes the verified debug APK and its SHA-256 checksum under `releases/`. No later phase was started.

## Reusable cloud configuration

Saved `install_script` and `start_skill` as an environment draft, plus the custom allowed domain `www.nhm.ac.uk` for editorial review. Saves are confirmed; publication and restoration in a fresh task have not been tested. Review and save the changes in environment settings, then publish the environment to retain the prepared snapshot. No runtime secrets are required for this game.

## Recommended next actions

1. Open in Android Studio or install the debug APK on a supported device; run the four Android tests and `docs/QA_CHECKLIST.md`.
2. Finish live museum reference verification when access is available.
3. Review/save/publish the reusable cloud configuration if retaining this environment.
4. Begin Phase 2 only after explicit instruction.
