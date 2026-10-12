#!/usr/bin/env python3
"""Generate bundled, offline Professor Fossil clips from the actual question bank.

Requires Python 3.12+, piper-tts==1.4.1, ffmpeg, and the verified VCTK model.
Run: /workspace/tooling/voice-venv/bin/python scripts/audio/generate_voice.py --model-dir /workspace/tooling/voice-model
The speech engine/model are development tools only; neither is shipped in the APK.
"""
import argparse
import hashlib
import json
import subprocess
import tempfile
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PIN = json.loads((ROOT / 'scripts/audio/voice_model.json').read_text())
OUT = ROOT / 'app/src/main/assets/audio/voice'
RENDERING = dict(codec='libvorbis', quality=2,
    filter='volume=0.65,apad=pad_dur=0.22,alimiter=limit=0.75:level=false')

SHARED = {
    'option_a': 'Option A.', 'option_b': 'Option B.', 'option_c': 'Option C.', 'option_d': 'Option D.',
    'correct': 'Roarsome! That is absolutely right!',
    'correct_alternate': 'Fantastic! You got it!',
    'retry': 'Good try, explorer! Let us think again.',
    'complete': 'Excellent work! You have earned a new dinosaur badge!',
    'final': 'You are a Dinosaur Expert! Five badges. Fifty discoveries. One roarsome explorer!',
    'welcome': 'Hello, explorer! I am Professor Fossil. Ready for a prehistoric adventure?',
}
# Spoken spellings help the English phonemiser with scientific names. On-screen
# spelling remains scientifically correct; both forms are recorded in the manifest.
PRONUNCIATIONS = {
    'Tyrannosaurus rex': 'Tyrannosaurus rex',
    'T. rex': 'Tyrannosaurus rex',
    'Pangaea': 'Pan jee uh',
    'Archaeopteryx': 'Ar key op ter icks',
}


def utterances(bank):
    for name, text in SHARED.items():
        yield f'shared/{name}.ogg', text
    for q in bank:
        yield f"{q['id']}/question.ogg", q['text']
        for choice in q['choices']:
            yield f"{q['id']}/{choice['id']}.ogg", choice['text']
        yield f"{q['id']}/explanation.ogg", q['explanation']
        if q.get('hint'):
            yield f"{q['id']}/hint.ogg", q['hint']


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--model-dir', type=Path, required=True)
    args = parser.parse_args()
    model = args.model_dir / 'en_GB-vctk-medium.onnx'
    config = Path(str(model) + '.json')
    assert hashlib.sha256(model.read_bytes()).hexdigest() == PIN['sha256'], 'Model checksum mismatch'
    assert hashlib.sha256(config.read_bytes()).hexdigest() == PIN['config_sha256'], 'Config checksum mismatch'
    import onnxruntime as ort
    ort.disable_telemetry_events()
    from piper import PiperVoice
    from piper.config import PiperConfig, SynthesisConfig
    from piper.voice import ESPEAK_DATA_DIR
    options = ort.SessionOptions()
    options.intra_op_num_threads = 2
    options.inter_op_num_threads = 1
    voice = PiperVoice(
        config=PiperConfig.from_dict(json.loads(config.read_text())),
        session=ort.InferenceSession(str(model), sess_options=options, providers=['CPUExecutionProvider']),
        espeak_data_dir=ESPEAK_DATA_DIR, download_dir=args.model_dir,
    )
    settings = SynthesisConfig(speaker_id=PIN['speaker_id'], length_scale=PIN['length_scale'],
        noise_scale=PIN['noise_scale'], noise_w_scale=PIN['noise_w_scale'])
    bank = json.loads((ROOT / 'app/src/main/assets/questions_en.json').read_text())
    previous_path = OUT / 'manifest.json'
    previous = json.loads(previous_path.read_text()) if previous_path.exists() else {}
    reusable = previous.get('clips', {}) if previous.get('voice') == PIN and previous.get('rendering') == RENDERING else {}
    records = {}
    total = list(utterances(bank))
    for index, (path, text) in enumerate(total, 1):
        dest = OUT / path
        spoken = text
        for word, spelling in PRONUNCIATIONS.items():
            spoken = spoken.replace(word, spelling)
        old = reusable.get(path)
        if old and old['text'] == text and old['spoken_text'] == spoken and dest.exists() and hashlib.sha256(dest.read_bytes()).hexdigest() == old['sha256']:
            records[path] = old
            continue
        dest.parent.mkdir(parents=True, exist_ok=True)
        with tempfile.TemporaryDirectory() as tmp:
            wav = Path(tmp) / 'clip.wav'
            with wave.open(str(wav), 'wb') as f:
                voice.synthesize_wav(spoken, f, settings)
            subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', str(wav), '-af',
                RENDERING['filter'], '-c:a', RENDERING['codec'], '-q:a', str(RENDERING['quality']),
                '-map_metadata', '-1', str(dest)], check=True)
        duration = float(subprocess.check_output(['ffprobe', '-v', 'error', '-show_entries',
            'format=duration', '-of', 'default=noprint_wrappers=1:nokey=1', str(dest)], text=True))
        records[path] = dict(text=text, spoken_text=spoken, sha256=hashlib.sha256(dest.read_bytes()).hexdigest(), duration=duration)
        print(f'Voice {index}/{len(total)}: {path}', flush=True)
    # Only remove obsolete generated Ogg clips; never touch other user assets.
    for old in reusable:
        if old not in records:
            (OUT / old).unlink(missing_ok=True)
    (OUT / 'manifest.json').write_text(json.dumps(dict(voice=PIN, rendering=RENDERING, license='CC-BY-4.0',
        attribution='CSTR VCTK Corpus 0.92, Christophe Veaux, Junichi Yamagishi and Kirsten MacDonald, University of Edinburgh; Piper VCTK medium model by rhasspy.',
        clips=records), ensure_ascii=False, indent=2) + '\n')
    print(f'Generated and indexed {len(records)} offline clips.', flush=True)


if __name__ == '__main__':
    main()
