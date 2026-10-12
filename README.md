# Dino Expert

An offline dinosaur learning adventure for Android phones and tablets. Professor Fossil guides players through **five levels and fifty curated questions**, with four shuffled answers, encouraging retries, and a discovery to read and hear after each correct answer.

**Phases 1–3 implemented.** The full game includes offline British English narration, five badges, replay, a final achievement, and responsive Compose graphics. Device QA and production release work remain in Phase 4. See [development status](DEVELOPMENT_STATUS.md).

## Download the game

[Download the latest debug APK (0.3.0)](releases/dino-expert-0.3.0-debug.apk) for Android 8.0 or newer. On GitHub, use the APK page's download button. The [SHA-256 checksum](releases/dino-expert-0.3.0-debug.apk.sha256) and [installation notes](releases/README.md) accompany the APK.

## Play

Tap **Play**, choose A–D, and try again until you find the correct answer. A wrong choice stays disabled; other choices stay available in the same order. Correct answers show a success icon and educational explanation. Tap **Next Question** yourself to advance; **Finish Level** earns a badge after question ten and unlocks the next adventure. Reopen the app and tap **Continue** to restore your exact question, answer order, wrong attempts, and any correct feedback. The level map shows locked, unlocked, completed and unfinished adventures. Completed levels can be replayed; switching to a replay preserves progress in other levels. Finishing Level 5 earns the Dinosaur Expert achievement. No points are lost, no ads or accounts exist, and there is no Internet permission.

Settings independently control soft chime effects, Professor Fossil’s voice, and reduced animation. Essential feedback is always visible as text and status symbols. Saves from the first MVP remain compatible, including existing badges and answer feedback.

## Professor Fossil’s voice

The APK contains **360 Ogg recordings**: every question, all 200 answer choices, explanations, hints, A–D labels and character feedback. A gentle British English voice, slightly slowed for clarity, accompanies the expressive character. Recordings are bundled; no network, device text-to-speech engine, paid service or voice download is needed to play.

The professor automatically reads a new question and its four choices in the same shuffled order shown on screen. Wrong answers trigger encouragement and a hint; only a correct answer triggers its discovery. **Read question and choices**, **Read the discovery again**, and **Stop reading** let players control playback. Voice completion never advances a question. Navigating away, backgrounding the app or losing audio focus stops narration. Automatic reading pauses when TalkBack is active; manual reading remains available.

Audio credits are available offline in Settings and in [asset licenses](docs/ASSET_LICENSES.md). [Audio generation instructions](docs/AUDIO.md) explain the verified development-only voice model and reproducible clip scripts.

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

Seven Android tests cover retry/explanation/Next interaction, two-column layout, real DataStore restoration, activity recreation and relaunch, locked levels, voice settings, and opening a bundled voice clip with Android’s decoder. Building the test APK does not execute those tests.

Validate all bundled recordings with FFmpeg installed:

```bash
python3 scripts/audio/validate_audio.py
```

The JVM suite also checks all 50 questions, five-level progression, replay/save migration, spoken choice ordering, and every voice asset’s text/hash/header coverage. Decoder validation checks all 360 recordings plus three effects for missing content, silence and clipping.

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

- `core/`: serializable questions/session/progress, bank validation, pure quiz transitions, safe save encoding/restoration, pure narration plans, JVM tests.
- `app/`: `MainActivity`, a lifecycle-aware `QuizViewModel`, a single DataStore, bundled questions, an Android audio-focus-aware recording queue, SoundPool chimes and a responsive Compose UI.
- `ProfessorFossilCharacter`: original Canvas artwork with idle, asking, thinking, celebrating, and congratulations states, plus a speaking mouth, gentle gestures and short celebrations. Character rendering is independent of quiz rules.
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

Questions and original explanations live in `app/src/main/assets/questions_en.json`. The JVM suite requires exactly fifty valid questions, ten per level, with unique IDs, four distinct answers, one designated correct answer, nonempty explanations, categories, difficulty and HTTPS source attribution. See [content review](docs/CONTENT_REVIEW.md) for the scientific review and reference retrieval results. Automated structure tests do not independently prove scientific truth.

Run the [manual acceptance checklist](docs/QA_CHECKLIST.md) before treating the game as device-validated or proceeding to a production release. Phase 4 remains outstanding.
