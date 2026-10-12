# Phases 2 and 3 implementation plan

Authorized together on 2026-10-12; implement and test the gameplay milestone before the visual/audio milestone.

## Phase 2

1. Preserve the original ten stable beginner questions and add forty curated questions, ten for each harder level. Check claims against museum sources and validate the complete bundled bank.
2. Extend saved progress with paused sessions for other levels and a pending celebration. Keep the version-1 schema backward compatible through optional/defaulted fields.
3. Add a width-adaptive level map, persistent badges, locked-level gating, replay, resumable adventures, next-level navigation and a final expert achievement.
4. Validate the actual 50-question asset and test five-level completion, locks, replay, paused session restoration, old MVP save migration and invalid saved state. Compile the app/test APK and run lint before Phase 3.

## Phase 3

1. Refine the original Canvas character with shading, explorer outfit details, expression/gesture changes and speaking motion. Add scenery, fossil decorations, five distinct illustrated badges, animated answer/progress states and short confetti celebrations.
2. Generate locally bundled voice for all questions, choices, discoveries and hints using a verified permissively licensed VCTK model. A warm, gently paced British English voice suits Professor Fossil. Keep speech engine/model outside the app; gameplay must remain offline.
3. Use stable answer-ID speech clips with separate A–D label clips so shuffling and retries cannot mislabel spoken options. Add Read/Stop controls, independent voice and sound settings, lifecycle/audio-focus cancellation and TalkBack-aware automatic narration.
4. Bundle original soft chimes with explicit asset licenses and readable offline credits.
5. Test source/voice coverage and incorrect/correct narration rules, decode every clip for integrity, silence and clipping, compile the final APK/test APK, run lint, verify signing and package contents, and update documentation and the GitHub APK.

## Remaining external validation

The cloud machine has no KVM and adb cannot write its actual home key/cache directory. Android tests are compiled but require a supported device. Physical listening/pronunciation, visual layouts, font scaling, rotation and TalkBack are Phase 4 validation tasks, not claimed as tested here. Six legacy NHM directory links also need a successful primary-page editorial recheck; other main references and supplementary AMNH pages are accessible.
