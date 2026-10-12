#!/usr/bin/env python3
"""Original soft bell/chime effects, dedicated to CC0; no external recordings."""
import array
import math
import wave
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RATE = 22050
patterns = {
    'correct': [(523.25, .12), (659.25, .12), (783.99, .24)],
    'retry': [(349.23, .20)],
    'complete': [(523.25, .12), (659.25, .12), (783.99, .12), (1046.50, .36)],
}
for name, notes in patterns.items():
    samples = array.array('h')
    for freq, duration in notes:
        for i in range(int(duration * RATE)):
            t = i / RATE
            envelope = min(1.0, t / .012) * math.exp(-t * 9) * min(1.0, (duration - t) / .035)
            value = math.sin(2 * math.pi * freq * t) + .18 * math.sin(2 * math.pi * freq * 2 * t)
            samples.append(round(value * envelope * 6200))
    samples.extend([0] * int(RATE * .05))
    with wave.open(str(ROOT / f'app/src/main/res/raw/{name}.wav'), 'wb') as wav:
        wav.setnchannels(1); wav.setsampwidth(2); wav.setframerate(RATE); wav.writeframes(samples.tobytes())
    print(f'Generated {name}.wav')
