# Professor Fossil audio

The app uses bundled local Ogg Vorbis speech and WAV chimes, not runtime synthesis. It runs offline even on Android devices without an installed speech voice. Questions and choices stay visible, and all gameplay works with voice and effects turned off.

## Playback

`core/NarrationPlan.kt` maps a question/session to local asset paths. It interleaves the four stable choice IDs with separate spoken Option A–D clips in **display order**. It never identifies the correct option before a correct selection, and never changes progress. A correct answer reads positive feedback, the accepted choice and its explanation; a wrong answer reads encouragement and a non-revealing hint. Manual Read controls can repeat the question or discovery.

`GameAudio.kt` manages one generation-guarded MediaPlayer queue and a SoundPool for short effects. Old callbacks cannot resume a cancelled queue. Screen changes, lifecycle changes, disabling narration, audio-focus loss and disposal stop speech. Media errors show a retry notice without blocking the quiz. TalkBack suppresses automatic narration to avoid two competing voices; manual playback remains available. Reduced animation suppresses speaking motion without affecting voice playback.

## Regenerate voice recordings

Ordinary Android builds **do not require** Python, Piper, the model, or any network connection to a voice service. The committed recordings and manifest are sufficient.

For audio editing on Linux with Python 3.12+ and FFmpeg:

```bash
python3 -m venv /workspace/tooling/voice-venv
/workspace/tooling/voice-venv/bin/pip install -r scripts/audio/requirements.txt
```

Download the model and its JSON configuration from the pinned Hugging Face revision in `scripts/audio/voice_model.json`, retaining TLS verification. Store them as:

```text
/workspace/tooling/voice-model/en_GB-vctk-medium.onnx
/workspace/tooling/voice-model/en_GB-vctk-medium.onnx.json
```

Then run:

```bash
/workspace/tooling/voice-venv/bin/python scripts/audio/generate_voice.py --model-dir /workspace/tooling/voice-model
python3 scripts/audio/generate_sfx.py
python3 scripts/audio/validate_audio.py
```

Generation rejects a model or configuration with a different SHA-256 from the verified pin. It reads the actual question bank, produces each question/choice/explanation/hint, and records source/spoken text, duration and SHA-256 for every clip. Unchanged, verified clips are reused. Changes to the voice or encoding settings invalidate reuse. Only obsolete generated Ogg paths from the previous manifest may be removed.

VCTK speaker `p226` uses length scale `1.08`, noise scale `0.5`, and noise-width scale `0.65`. Recordings are lowered to 65% gain before limiting and encoding so lossy decoding has headroom. The final validator checks **all 360 speech clips and three chimes**, including actual FFmpeg decoding, non-silence, duration and peak levels. This caught excessive decoded peaks in the initial generation; all clips were regenerated with headroom and passed revalidation.

Scientific name pronunciation and subjective voice appeal still require human listening on a supported device before production release. The voice uses a licensed VCTK speaker styled for the original character. See [asset licenses](ASSET_LICENSES.md).
