# Phase 1 device acceptance checklist

These are remaining device checks, not claims of completed testing. Use an emulator or physical test device; keep any personal app save if testing on an existing installation.

## Automated device tests

With a supported emulator/device connected:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Inspect `app/build/reports/androidTests/connected/debug/index.html`. Four Android tests must execute, not merely compile. Test code preserves the prior DataStore save where it changes persistent state.

## Play and restore

- Launch, tap Play. Exactly four distinct A–D options appear.
- Choose a wrong answer. Question/count/order stay unchanged; only that option disables and displays “Try another”. Correct answer is not highlighted.
- Choose all three wrong answers. The remaining correct answer is still enabled.
- Choose correctly. Success icon/text and an explanation appear; no automatic advance occurs.
- Double tap answers and Next. Advance exactly once, never skip a question.
- Go Home and Continue. Resume the same session.
- Answer all ten correctly and use Finish Level. Beginner badge appears; replay begins at question one without deleting the badge.
- Force-stop/relaunch after a wrong answer and after a correct answer. Resume and verify wrong choices, answer order, explanation, and settings persist. For a disposable emulator:

```bash
adb shell am force-stop com.dinoexpert
adb shell am start -n com.dinoexpert/.MainActivity
```

- Enable airplane mode before launch and play the entire level. No network is required.

## Layout and accessibility

- Small phone (~320dp width), typical phone (~411dp), tablet (~800dp): portrait and landscape.
- Resize/split screen across 600dp. Answer grid switches between one and two columns without changing answer order or state.
- Rotate on an unanswered, incorrect, and correct question. Progress and enabled/disabled choices persist.
- Raise font scale to 1.5× and 2×. Scroll to every answer, feedback and Next; avoid clipping or inaccessible controls.
- Use a device with system bars/cutouts. No controls overlap them.
- Enable TalkBack. Check question heading, A–D labels, correctness/retry text, disabled states, settings switches and character description. Confirm feedback announcements and sensible focus order.
- Switch sound off. All gameplay feedback remains understandable. Reopen and confirm setting persists.
- Enable reduced animation, and separately disable system animations. Professor stays still; gameplay still works.

## Deferred final-game acceptance

Locked-level UI, five-level unlocking, final achievement and fifty-question validation belong to Phase 2 onward. They are not Phase 1 capabilities.
