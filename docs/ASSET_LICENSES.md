# Bundled asset credits and licenses

These licenses apply to the specified assets; they do not license the entire application source.

## Professor Fossil voice recordings — CC BY 4.0

`app/src/main/assets/audio/voice/**/*.ogg` contains original game text synthesized for Dino Expert using the Piper VCTK medium model, speaker `p226`. Delivery uses a slightly slower pace and adjusted synthesis settings; pronunciation substitutions and audio gain/headroom processing are applied before Ogg encoding. The voice is designed as a gentle British English companion to the original Professor Fossil character.

Source: **CSTR VCTK Corpus, version 0.92 (2019)**, Christophe Veaux, Junichi Yamagishi and Kirsten MacDonald, University of Edinburgh.

- Corpus: https://datashare.ed.ac.uk/handle/10283/3443
- Corpus/recording license: https://creativecommons.org/licenses/by/4.0/
- Voice model: https://huggingface.co/rhasspy/piper-voices/tree/c10ece1aade47bb51c153c893d14e5bf8e5b7117/en/en_GB/vctk/medium
- Source model card identifies the corpus’s CC BY 4.0 license. Base training uses the public-domain LJSpeech/lessac corpus.
- Changes: original game text, speaker selection, synthesis pacing/noise settings, pronunciation substitutions, gain reduction, padding, limiting and Ogg compression. Source authors do not endorse this game.

Bundled generated voice recordings are offered under **Creative Commons Attribution 4.0 International**. Preserve this attribution and the license link when distributing them. Credits also ship in `app/src/main/assets/audio/CREDITS.txt` and are readable offline from Settings.

Piper is GPL-3.0 generation software used only during development. Neither Piper, ONNX Runtime nor the pretrained model is included in the APK. Gameplay plays local recordings with Android MediaPlayer.

## Sound effects — CC0

`app/src/main/res/raw/correct.wav`, `retry.wav` and `complete.wav` are original soft synthesized chimes. Their generation source is `scripts/audio/generate_sfx.py`; no external recordings were used. These three audio recordings are dedicated under **CC0 1.0 Universal**: https://creativecommons.org/publicdomain/zero/1.0/

## Original graphics

Professor Fossil, fossil/leaf/hourglass/magnifier/star badges, mountains, trails, fossil decorations and confetti are original Compose Canvas drawings. No proprietary graphics, downloaded font files or external animation assets are bundled.
