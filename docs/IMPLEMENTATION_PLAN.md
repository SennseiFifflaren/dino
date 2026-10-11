# Phase 0 — technical plan

The repository is initially empty. Implement Phase 1 only; further phases require explicit authorization.

## Stack and structure

- Kotlin 2.2.21, Compose compiler plugin of the same version.
- Android Gradle Plugin 8.13.2, Gradle 8.13, Java toolchain 21 (bytecode 17).
- compile/target SDK 36, min SDK 26; Android 16 edge-to-edge supported.
- Compose BOM 2025.10.01, Material 3; activity-compose 1.11.0.
- Lifecycle 2.9.4 (ViewModel, lifecycle-aware StateFlow collection), DataStore 1.1.7.
- Kotlin serialization 1.9.0 and coroutines 1.10.2; JUnit 4.13.2.
- `app`: Android entry point, local data/persistence, MVVM, theme, screens, Canvas character.
- `core`: Android-independent models, content validation, save codec, deterministic game rules and JVM tests.
- `app/src/main/assets/questions_en.json`: ten original beginner questions, with attribution.
- `app/src/main/res/values/strings.xml`: UI copy; future translations can use resource qualifiers. Question banks can be selected by locale in a future phase.

## State and transitions

`QuizSession` stores level, question index, shuffled stable choice IDs, attempted wrong IDs, and answered status. `PlayerProgress` also stores completed levels, highest unlocked level, active session, and settings. Validate loaded saves against the content; fall back safely for invalid data. A serial ViewModel action processor writes every accepted transition to DataStore before publishing it, preventing duplicate actions and stale save writes.

Menu → Play/Continue → Question → explicit Next Question → next question or Level Complete. Menu and settings are reachable without losing the session. Correct feedback is retained until Next, including after process restart. The MVP has only Level 1; Phase 2 will add level selection, additional levels, unlock/replay UI, and final achievement. The engine will support a bank of multiple curated levels without adding those screens now.

## Required milestones and checks

1. Pure rules and curated ten-question bank; test retry stability, disabled wrong choices, correct gating, duplicate prevention, completion, replay, restoration, malformed saves and content validation.
2. Playable Compose UI, character states and responsive layout, serialized persistent progress.
3. Compile a debug APK, execute JVM tests and Android lint. Build Android UI tests; run on a device/emulator if supported.
4. Document commands, tested evidence, untested device checks, and remaining phases.

## Initial environment

Java 21 is present. Gradle, Android SDK and adb are initially absent. Downloads are reachable; install tooling outside the checkout. No credentials or runtime network services are needed for gameplay. Confirm emulator capability before claiming device validation. Do not fabricate runtime or accessibility results.
