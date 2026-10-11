# Dino Expert

An offline dinosaur learning adventure for Android phones and tablets. Professor Fossil guides players through ten beginner questions with four shuffled answers, encouraging retries, and a fact to read after each correct answer.

**Phase 1 MVP only.** Levels 2–5 and the fifty-question game are intentionally deferred. See [development status](DEVELOPMENT_STATUS.md) and the [Phase 0 plan](docs/IMPLEMENTATION_PLAN.md).

## Download the MVP

[Download the Phase 1 debug APK](releases/dino-expert-0.1.0-debug.apk) for Android 8.0 or newer. On GitHub, use the APK page's download button. The [SHA-256 checksum](releases/dino-expert-0.1.0-debug.apk.sha256) and [installation notes](releases/README.md) accompany the APK.

## Play

Tap **Play**, choose A–D, and try again until you find the correct answer. A wrong choice stays disabled; other choices stay available in the same order. Correct answers show a success icon and educational explanation. Tap **Next Question** yourself to advance; **Finish Level** earns the beginner badge after question ten. Reopen the app and tap **Continue** to restore your exact question, answer order, wrong attempts, and any correct feedback. Completed Level 1 can be replayed. No points are lost, no ads or accounts exist, and there is no Internet permission.

Settings control gentle synthesized answer tones and reduced animation. Essential feedback is always visible as text and status symbols.

## Open in Android Studio

1. Open this repository folder, rather than `app/` alone, in a current stable Android Studio supporting AGP 8.13.2.
2. Choose a full **JDK 21** for the Gradle JDK (the Android Studio bundled JDK is suitable if it is version 21). A Java runtime without `javac` is insufficient.
3. Install **Android SDK Platform 36**, **Build-Tools 36.0.0**, and **Platform-Tools** from SDK Manager. Let Studio create your ignored `local.properties`, or set `ANDROID_HOME` to your SDK location.
4. Sync Gradle, select the `app` configuration, and run on an Android 8.0/API 26 or newer device or emulator.

The app supports portrait and landscape; widths of at least 600dp use a side-by-side character/question panel and two answer columns. Smaller windows use a vertical scrolling layout. System bars and cutouts are respected.

## Command-line build and tests

Use JDK 21 and your SDK location:

```bash
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/android-sdk
./gradlew :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug
```

Windows: use `gradlew.bat` with equivalent environment settings. The Gradle 8.13 wrapper includes distribution checksum verification.

Debug APK: `app/build/outputs/apk/debug/app-debug.apk`.

JVM results: `core/build/reports/tests/test/index.html`.

Lint report: `app/build/reports/lint-results-debug.html`.

On a connected device/emulator:

```bash
adb devices
./gradlew :app:connectedDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.dinoexpert/.MainActivity
```

Android tests cover retry/explanation/Next interaction, two-column layout, real DataStore restoration, activity recreation, and activity relaunch. Building the test APK does not execute those tests.

## This cloud workspace

Tooling is installed outside the checkout under `/workspace/tooling`. To initialize or refresh verified tool downloads, proxy configuration, and the Java trust store:

```bash
bash scripts/cloud-setup.sh
source scripts/cloud-env.sh
./gradlew :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-daemon
```

The cloud helper targets Linux x86_64 with Python 3.12+, curl, and the supplied cloud CA bundle when present. It pins Temurin JDK 21.0.12.1+1 and Android command-line tools 19.0. SDK archives and JDK/Gradle distributions retain their official integrity checks, and TLS verification stays enabled. The helper does not overwrite application sources or local SDK settings. Subsequent tasks use this existing checkout; no extra Git worktree is needed.

The current machine has no KVM acceleration. `adb` also requires a writable user `.android` directory, which this machine does not provide even with an additional permission grant. Device tests and visual QA must run in Android Studio or another compatible machine. Actual build/test outcomes are recorded in [development status](DEVELOPMENT_STATUS.md).

## Architecture

- `core/`: serializable questions/session/progress, bank validation, pure quiz transitions, safe save encoding/restoration, JVM tests.
- `app/`: `MainActivity`, a lifecycle-aware `QuizViewModel`, a single DataStore, bundled questions and a responsive Compose UI.
- `ProfessorFossilCharacter`: original Canvas artwork with idle, asking, thinking, celebrating, and congratulations states. Character rendering is independent of quiz rules.
- `app/src/main/res/values/strings.xml`: localizable UI text. The English bank uses stable question and answer IDs so future translated banks can keep progress compatible.

The ViewModel processes actions serially and persists accepted changes before showing them. Double answers, duplicate Next events, and unknown choice IDs do not award or advance progress. Writes that fail leave the last saved state visible with a retry message. Corrupted JSON/preferences recover to safe defaults; invalid sessions are discarded while valid settings and completions survive.

## Dependency versions

| Component | Version |
| --- | --- |
| Kotlin / Compose compiler plugin | 2.2.21 |
| Android Gradle Plugin / Gradle | 8.13.2 / 8.13 |
| Compile / target / minimum SDK | 36 / 36 / 26 |
| Build tools | 36.0.0 |
| Compose BOM / Material 3 | 2025.10.01 / BOM managed |
| Activity / Core KTX | 1.11.0 / 1.17.0 |
| Lifecycle / DataStore | 2.9.4 / 1.1.7 |
| Coroutines / Kotlin serialization | 1.10.2 / 1.9.0 |
| JUnit | 4.13.2 |

## Content and manual QA

Questions and original explanations live in `app/src/main/assets/questions_en.json`. The JVM suite requires ten valid Level 1 questions with unique IDs, four distinct answers, one designated correct answer, nonempty explanations, categories, difficulty and HTTPS source attribution. See [content review](docs/CONTENT_REVIEW.md) for the scientific review and reference-access limitation. Automated structure tests do not independently prove scientific truth.

Run the [manual acceptance checklist](docs/QA_CHECKLIST.md) before treating this MVP as device-validated or proceeding to a release. The final game still requires fifty curated questions and the remaining development phases.
