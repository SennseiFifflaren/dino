#!/usr/bin/env python3
"""Check source coverage, SHA-256, real decoder output and non-silent audio."""
import array
import hashlib
import json
import math
import subprocess
from pathlib import Path
from generate_voice import ROOT, utterances

voice = ROOT / 'app/src/main/assets/audio/voice'
manifest = json.loads((voice / 'manifest.json').read_text())['clips']
bank = json.loads((ROOT / 'app/src/main/assets/questions_en.json').read_text())
expected = dict(utterances(bank))
assert set(expected) == set(manifest), 'Missing or unexpected voice clips'
files = []
for path, text in expected.items():
    p = voice / path
    assert manifest[path]['text'] == text, f'Stale speech text: {path}'
    assert hashlib.sha256(p.read_bytes()).hexdigest() == manifest[path]['sha256'], f'Corrupt voice clip: {path}'
    files.append(p)
files += list((ROOT / 'app/src/main/res/raw').glob('*.wav'))
for index, p in enumerate(files, 1):
    pcm = subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(p), '-f', 'f32le', '-ac', '1', '-ar', '22050', '-'])
    samples = array.array('f', pcm)
    assert len(samples) > 2205, f'Audio too short: {p}'
    peak = max(abs(v) for v in samples)
    rms = math.sqrt(sum(v * v for v in samples) / len(samples))
    assert peak > .03 and rms > .003, f'Silent or inaudible audio: {p}'
    assert peak < .9999, f'Clipped audio: {p}'
    if index % 50 == 0:
        print(f'Decoded and checked {index}/{len(files)} files', flush=True)
print(f'PASS: {len(expected)} matching voice clips and {len(files)-len(expected)} sound effects decode without silence or clipping.', flush=True)
