# Device acceptance checklist — full game and voice

These checks remain for a supported emulator or physical device. Preserve any existing player save when testing on a personal device. Automated builds are not proof of device usability.

## Automated device tests

```bash
./gradlew :app:connectedDebugAndroidTest
```

Inspect `app/build/reports/androidTests/connected/debug/index.html`. **Seven** Android tests must execute, not merely compile. Tests cover quiz feedback/Next, two-column layout, DataStore, activity recreation/relaunch, locked levels, voice settings and local speech decoding. Tests that change persistence restore the prior save afterwards.

## All five levels

- Begin with only Dino Beginner unlocked. Locked cards are disabled; levels cannot be opened prematurely.
- Every question has four distinct A–D options. An incorrect answer retains question, count and order, disables only that choice, and keeps other choices available.
- Try all three wrong answers. The correct choice remains available. No progress or rewards are lost.
- A correct choice shows its icon, success message and educational explanation. It never advances automatically, even when narration finishes.
- Double tap answers and Next. Exactly one completion/advance occurs.
- Finish ten questions per level. Earn its badge and unlock the next level. Repeat through Dinosaur Expert; final achievement has all five badges.
- Replay a completed level. Badges and unlocked levels stay earned.
- Pause an unfinished Level 2 and replay Level 1. Return to Level 2: question, answer order, wrong attempts and correct-feedback status must be unchanged.
- Close on a celebration screen and reopen. Celebration is restored; explicit navigation dismisses it.
- Install the 0.3.0 update over the original 0.1.0 using `adb install -r`. Existing beginner progress/settings and the badge remain; a completed beginner level unlocks Explorer.

## Offline and lifecycle

- Enable airplane mode before launching. Play every level, replay, and use Read controls. No network or installed text-to-speech voice should be needed.
- Force-stop after an incorrect and after a correct answer; reopen and Continue. Persist the exact state and settings:

```bash
adb shell am force-stop com.dinoexpert
adb shell am start -n com.dinoexpert/.MainActivity
```

- Background the app during narration; sound stops. Return and check current content/progress. Audio-focus interruptions must cancel reading without changing progress.
- Rotate on unanswered, incorrect and correct questions; keep state and choice order.

## Professor Fossil audio

- Listen to every question, all four choices, each explanation and hint. Check scientific-name pronunciations, clarity, volume and a friendly character voice.
- Spoken Option A–D labels must match the shuffled choices visible on screen, including after restart and retries.
- A wrong answer reads encouragement and a non-revealing hint, never the correct option or explanation.
- A correct answer reads the accepted choice and explanation. The player still presses Next/Finish.
- Read repeats the current question/discovery; Stop ends the queue. Quickly change screens, press Next or toggle voice during playback; no stale clips should continue.
- Turn voice off: narration stops immediately and stays off after reopening. Chimes can be independently enabled/disabled.
- Open Settings credits offline and verify attribution is present.

## Layout and accessibility

- Small phone (~320dp), typical phone (~411dp), tablet (~800dp), in portrait and landscape.
- Resize/split screen across 600dp. Questions/character switch layout; answers switch one/two columns without state changes.
- Font scale 1.5× and 2×: all answers, facts, reading controls and Next remain reachable by scrolling.
- System bars/cutouts never overlap controls; touch targets stay at least 48dp.
- TalkBack: question heading, four labels, disabled states, retry/correct text, reading controls, badge descriptions and settings switches have meaningful focus order. Automatic Professor narration is suppressed; manual Read is still available.
- Reduced animation and disabled system animations keep Professor, color/progress transitions and confetti still. Audio stays independently configurable.
- Sound/voice both off: every essential fact and gameplay status remains understandable.

## Phase 4 remains

Complete this device matrix and final editorial/listening review before release signing and distribution QA. No production release or Play Store upload has been performed.
