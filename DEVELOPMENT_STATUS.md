# Development status

## Current release: 0.3.0 — Phases 1–3 implemented

The five-level game and offline Professor Fossil narration are implemented and build-validated. Physical-device/UI/listening QA and production release work remain in Phase 4. See [README](README.md), [extension plan](docs/PHASES_2_3_PLAN.md), and [manual QA](docs/QA_CHECKLIST.md).

## Phase 0 and Phase 1 — completed implementation milestones

The originally empty repository became the first playable ten-question MVP. Its Gradle wrapper, Kotlin/Compose architecture, pure quiz rules, DataStore persistence, Canvas character and debug APK were validated. Fourteen JVM tests passed for that milestone. The original `0.1.0` APK remains under `releases/`.

## Phase 2 — implemented and milestone tested

- [x] Five named levels and fifty curated questions, ten per level, with explanations, hints and museum attribution.
- [x] Adaptive level map with backend and UI locked-level gating.
- [x] Sequential unlocks, permanent badges, completed-level replay and final expert achievement.
- [x] Stable shuffled choices, unlimited retries, disabled wrong choices, explicit Next and duplicate prevention retained.
- [x] Paused sessions preserve unfinished adventures when switching levels/replaying.
- [x] Pending celebrations restore after reopening; version-1 MVP saves remain compatible.
- [x] Bank validation and twenty gameplay/content/save tests passed before Phase 3.
- [x] Phase 2 debug APK, Android test APK and lint passed.

## Phase 3 — implemented and build/audio tested

- [x] Refined original Professor Fossil drawing: shaded skin/outfit, expressive eyes, gestures, breathing/blinking, speaking mouth and success reactions.
- [x] Prehistoric mountains/trail/fossil scenery, five illustrated badge designs, animated answer/progress feedback and short confetti.
- [x] Three original, licensed soft chime recordings.
- [x] 360 bundled British English voice clips: all questions, 200 choices, explanations, hints, Option A–D labels and character dialogue.
- [x] Spoken choice order follows the shuffled stable IDs; wrong feedback does not reveal the answer; narration never advances the game.
- [x] Read/Stop controls, separate voice/effects settings, lifecycle/audio-focus cancellation and TalkBack-aware automatic narration.
- [x] Reduced-motion support, scrollable phone/tablet layouts, system insets, scalable fonts, readable locked cards and 48dp+ targets.
- [x] Offline audio credits, provenance/checksums, generation scripts and asset licenses.
- [x] Fresh final run: 25 JVM tests passed with zero failures, errors or skips.
- [x] All 360 speech clips and three chimes actually decoded and checked for source coverage, silence and clipping. Initial decoder peaks were fixed by regenerating with headroom; final checks passed.
- [x] Final debug and Android test APKs built; lint passed with zero errors and six dependency-update advisories. Tested stable dependencies remain pinned.
- [x] APK signing verified; old/new APK certificates match, allowing in-place updates without uninstalling the MVP.
- [x] Packaged bank and all speech files match source assets; all Ogg entries are uncompressed for Android `openFd`; no Internet permission or speech model is bundled.
- [ ] Seven Android tests compiled but remain unrun on this cloud machine.
- [ ] Device visual, orientation, font scaling, TalkBack, playback/phonetic and subjective voice review.

## Validation — 2026-10-12

```bash
source scripts/cloud-env.sh
./gradlew :core:test :app:assembleDebug :app:assembleDebugAndroidTest :app:lintDebug --no-daemon
python3 scripts/audio/validate_audio.py
```

The core test task explicitly tracks the Android asset directory, so changing the question bank or voice files invalidates cached test results. The final 25-test run was executed against the current recordings and bank.

Latest APK: `releases/dino-expert-0.3.0-debug.apk` (18,222,482 bytes), with a matching SHA-256 file. Reports are in `core/build/reports/tests/test/` and `app/build/reports/lint-results-debug.*`.

## Known limitations and remaining Phase 4

The machine has no KVM acceleration, and adb cannot create its actual user home key/cache directory on the read-only home filesystem. Device tests are built, not executed. Use Android Studio or a supported physical device for the seven tests and manual checklist; no device playback or visual/accessibility success is claimed.

The main museum articles and several directory pages are now reachable and were inspected. Six legacy NHM directory links timed out; supplementary AMNH anatomy pages were retrieved. Their primary-page availability remains for editorial recheck, documented in [content review](docs/CONTENT_REVIEW.md). Scientific uncertainty is phrased explicitly in the bank.

Phase 4 release QA, production signing and Play Store distribution have not been performed. No ads, accounts, purchases, analytics, online databases or personal information were added.

## Reusable cloud development

JDK/SDK/Gradle tools remain outside the checkout under `/workspace/tooling`. Run the cloud helper to initialize proxy/CA settings in a new task, then build and validate the committed assets. Voice-model tooling is needed only when regenerating speech. Existing task checkouts are isolated; no additional worktree is needed.
