"""The one audio generator for this app. Swap the voice here and every clip changes.

Reads content (words, phrases, songs) and writes mono, loudness-normalised Ogg Vorbis files:
  app/src/main/assets/audio/words/<id>.ogg
  app/src/main/assets/audio/phrases/<id>.ogg
  app/src/main/assets/audio/songs/<id>.ogg   (+ word timings written back into songs.json)

Voice backend: the Windows built-in speech engine (System.Speech, "Microsoft Zira Desktop").
These are PLACEHOLDER recordings; see D-013 before a public release.

Requirements: Windows with PowerShell, Python 3.10+, `pip install soundfile numpy`.
Usage: python scripts/gen_audio.py [--force] [--voice "Microsoft Zira Desktop"] [--rate -1]
"""
from __future__ import annotations

import argparse
import hashlib
import json
import math
import pathlib
import subprocess
import sys
import tempfile

import numpy as np
import soundfile as sf

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets"
CONTENT = ASSETS / "content"
CACHE_FILE = ROOT / "scripts/audio_cache.json"
RATE = 22050
PROCESS_VERSION = "3"  # bump to force regeneration after changing processing
TARGET_RMS_DB = -19.0
PEAK_LIMIT = 0.89

PS_SCRIPT = r"""
param([string]$Jobs, [string]$Voice, [int]$Rate)
$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Speech
$synth = New-Object System.Speech.Synthesis.SpeechSynthesizer
$synth.SelectVoice($Voice)
$synth.Rate = $Rate
$format = New-Object System.Speech.AudioFormat.SpeechAudioFormatInfo(22050, [System.Speech.AudioFormat.AudioBitsPerSample]::Sixteen, [System.Speech.AudioFormat.AudioChannel]::Mono)
foreach ($job in (Get-Content -Raw -Encoding UTF8 $Jobs | ConvertFrom-Json)) {
    $synth.SetOutputToWaveFile($job.out, $format)
    $synth.Speak([string]$job.text)
    $synth.SetOutputToNull()
}
$synth.Dispose()
"""


# ------------------------------------------------------------------ helpers
def load(name):
    return json.loads((CONTENT / name).read_text(encoding="utf-8"))


def synthesize(texts: list[str], voice: str, rate: int) -> list[np.ndarray]:
    """Speaks each text with the Windows voice and returns raw float32 mono arrays."""
    if not texts:
        return []
    with tempfile.TemporaryDirectory() as tmp:
        tmpdir = pathlib.Path(tmp)
        jobs = [{"text": t, "out": str(tmpdir / f"{i}.wav")} for i, t in enumerate(texts)]
        (tmpdir / "jobs.json").write_text(json.dumps(jobs), encoding="utf-8")
        (tmpdir / "speak.ps1").write_text(PS_SCRIPT, encoding="utf-8")
        subprocess.run(
            ["powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", str(tmpdir / "speak.ps1"),
             "-Jobs", str(tmpdir / "jobs.json"), "-Voice", voice, "-Rate", str(rate)],
            check=True,
        )
        out = []
        for job in jobs:
            data, sr = sf.read(job["out"], dtype="float32")
            assert sr == RATE, sr
            out.append(data if data.ndim == 1 else data.mean(axis=1))
        return out


def trim(x: np.ndarray, threshold=0.012, pad_s=0.04) -> np.ndarray:
    idx = np.where(np.abs(x) > threshold)[0]
    if len(idx) == 0:
        return x
    pad = int(pad_s * RATE)
    return x[max(0, idx[0] - pad): min(len(x), idx[-1] + pad)]


def normalise(x: np.ndarray) -> np.ndarray:
    rms = math.sqrt(float(np.mean(x ** 2))) or 1e-9
    x = x * (10 ** (TARGET_RMS_DB / 20) / rms)
    peak = float(np.max(np.abs(x))) or 1e-9
    if peak > PEAK_LIMIT:
        x = x * (PEAK_LIMIT / peak)
    return x


def fade(x: np.ndarray, ms=12) -> np.ndarray:
    n = min(len(x) // 2, int(RATE * ms / 1000))
    if n > 0:
        ramp = np.linspace(0, 1, n, dtype=np.float32)
        x = x.copy()
        x[:n] *= ramp
        x[-n:] *= ramp[::-1]
    return x


def write_ogg(path: pathlib.Path, x: np.ndarray):
    path.parent.mkdir(parents=True, exist_ok=True)
    sf.write(str(path), np.clip(x, -1, 1).astype(np.float32), RATE, format="OGG", subtype="VORBIS")


def key(*parts) -> str:
    return hashlib.sha1("|".join(map(str, parts)).encode()).hexdigest()


# ------------------------------------------------------------------ songs
NOTE = {n: 440 * 2 ** ((i - 9) / 12) for i, n in enumerate(["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"])}


def tone(freq: float, dur: float, vol=0.10) -> np.ndarray:
    t = np.arange(int(dur * RATE)) / RATE
    env = np.minimum(1, t / 0.02) * np.exp(-3.0 * t)
    wave = np.sin(2 * np.pi * freq * t) + 0.3 * np.sin(4 * np.pi * freq * t)
    return (vol * env * wave).astype(np.float32)


def build_song(song: dict, voice: str, rate: int) -> tuple[np.ndarray, list[dict]]:
    """A gentle spoken chant: one sung token per beat over a soft plucked melody.

    Each token is spoken separately and placed on its beat, so the word timings are exact.
    Returns the mixed audio and the list of {line, token, startMs, endMs}.
    """
    beat = 60.0 / song.get("bpm", 92)
    melody = song.get("melody", ["C", "E", "G", "E"])
    tokens = [(li, ti, tok) for li, line in enumerate(song["lines"]) for ti, tok in enumerate(line["tokens"])]
    clips = synthesize([tok["text"] for _, _, tok in tokens], voice, rate)
    lead_in = 2 * beat
    total_beats = sum(len(line["tokens"]) + line.get("restBeats", 1) for line in song["lines"])
    length = int((lead_in + total_beats * beat + 1.5) * RATE)
    mix = np.zeros(length, dtype=np.float32)
    timings = []
    pos_beat = 0
    note_i = 0
    for b in range(2):  # count-in
        s = int(b * beat * RATE)
        c = tone(NOTE["C"] / 2, beat * 0.9, 0.08)
        mix[s: s + len(c)] += c[: max(0, length - s)]
    current_line = -1
    for (li, ti, tok), clip in zip(tokens, clips):
        if li != current_line:
            if current_line >= 0:
                pos_beat += song["lines"][current_line].get("restBeats", 1)
            current_line = li
        start_s = lead_in + pos_beat * beat
        voice_clip = fade(normalise(trim(clip)))
        max_len = int(beat * 1.6 * RATE)
        voice_clip = fade(voice_clip[:max_len])
        s = int(start_s * RATE)
        mix[s: s + len(voice_clip)] += voice_clip * 0.9
        note = tone(NOTE[melody[note_i % len(melody)]] / 2, beat * 0.95)
        mix[s: s + len(note)] += note[: max(0, length - s)]
        note_i += 1
        timings.append({"line": li, "token": ti, "startMs": int(start_s * 1000), "endMs": int((start_s + beat) * 1000)})
        pos_beat += 1
    # Trim only the tail: the timings are measured from the very start of the file.
    loud = np.where(np.abs(mix) > 0.002)[0]
    if len(loud):
        mix = mix[: min(len(mix), loud[-1] + int(0.3 * RATE))]
    peak = float(np.max(np.abs(mix))) or 1e-9
    mix = mix * (PEAK_LIMIT / peak) if peak > PEAK_LIMIT else mix
    return mix, timings


# ------------------------------------------------------------------ main
def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--force", action="store_true")
    ap.add_argument("--voice", default="Microsoft Zira Desktop")
    ap.add_argument("--rate", type=int, default=-1)
    args = ap.parse_args()

    cache = json.loads(CACHE_FILE.read_text(encoding="utf-8")) if CACHE_FILE.exists() else {}
    jobs = []  # (out path, text)
    for w in load("words.json")["words"]:
        jobs.append((ASSETS / w["audio"], w["text"].capitalize() + "."))
    for p in load("phrases.json")["phrases"]:
        jobs.append((ASSETS / p["audio"], p["text"]))

    todo = []
    for out, text in jobs:
        k = key(text, args.voice, args.rate, PROCESS_VERSION)
        rel = out.relative_to(ASSETS).as_posix()
        if args.force or cache.get(rel) != k or not out.exists():
            todo.append((out, text, rel, k))
    print(f"{len(jobs)} clips, {len(todo)} to generate")
    for start in range(0, len(todo), 40):
        batch = todo[start: start + 40]
        for (out, _, rel, k), raw in zip(batch, synthesize([t for _, t, _, _ in batch], args.voice, args.rate)):
            write_ogg(out, fade(normalise(trim(raw))))
            cache[rel] = k
        print(f"  {min(start + 40, len(todo))}/{len(todo)}")

    songs_path = CONTENT / "songs.json"
    if songs_path.exists():
        songs_doc = json.loads(songs_path.read_text(encoding="utf-8"))
        changed = False
        for song in songs_doc["songs"]:
            spec = json.dumps({k: song[k] for k in ("lines", "bpm", "melody") if k in song}, sort_keys=True)
            spec = json.dumps([[t["text"] for t in line["tokens"]] for line in song["lines"]]) + spec
            k = key(spec, args.voice, args.rate, PROCESS_VERSION)
            out = ASSETS / song["audio"]
            rel = out.relative_to(ASSETS).as_posix()
            if args.force or cache.get(rel) != k or not out.exists() or "timings" not in song:
                print(f"  song {song['id']}")
                mix, timings = build_song(song, args.voice, args.rate)
                write_ogg(out, mix)
                song["timings"] = timings
                cache[rel] = k
                changed = True
        if changed:
            songs_path.write_text(json.dumps(songs_doc, indent=2) + "\n", encoding="utf-8")

    CACHE_FILE.write_text(json.dumps(dict(sorted(cache.items())), indent=1) + "\n", encoding="utf-8")
    write_index()
    print("done")


def write_index():
    """assets/audio/index.json: duration in ms of every clip, so the app can play clips back to back."""
    index = {}
    for f in sorted((ASSETS / "audio").rglob("*.ogg")):
        index[f.relative_to(ASSETS).as_posix()] = int(round(sf.info(str(f)).duration * 1000))
    (ASSETS / "audio/index.json").write_text(json.dumps(index, indent=1) + "\n", encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())
