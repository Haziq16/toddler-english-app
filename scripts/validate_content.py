"""Validates everything in app/src/main/assets/content/.

Checks:
  * ids are unique and well-formed (words, phrases, scenes, games, songs)
  * every audio file and every image drawable exists
  * tier is 1, 2 or 3; scene ids exist; translations are {locale: text}
  * scene layouts, game decks and song tokens only reference real words
  * every word appears in at least MIN_USES places (scene + games + songs)

Usage: python scripts/validate_content.py [--lenient]
--lenient turns the usage rule into a warning (only for work in progress).
Exit code 0 when valid, 1 when not.
"""
from __future__ import annotations

import json
import pathlib
import re
import sys
from collections import Counter, defaultdict

ROOT = pathlib.Path(__file__).resolve().parent.parent
ASSETS = ROOT / "app/src/main/assets"
CONTENT = ASSETS / "content"
DRAWABLE = ROOT / "app/src/main/res/drawable"
ID = re.compile(r"^[a-z][a-z0-9_]*$")
LOCALE = re.compile(r"^[a-z]{2,3}(-[A-Z]{2})?$")
MIN_USES = 2
MIN_TIER1_PER_SCENE = 4
GAME_TYPES = {"bubbles", "match", "singalong", "feed"}


class Report:
    def __init__(self):
        self.errors: list[str] = []
        self.warnings: list[str] = []

    def error(self, msg):
        self.errors.append(msg)

    def warn(self, msg):
        self.warnings.append(msg)


def load(rep: Report, name: str, required=True):
    path = CONTENT / name
    if not path.exists():
        if required:
            rep.error(f"{name}: missing")
        return None
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except json.JSONDecodeError as e:
        rep.error(f"{name}: invalid JSON ({e})")
        return None


def check_audio(rep, where, rel):
    if not isinstance(rel, str) or not rel.endswith(".ogg"):
        rep.error(f"{where}: audio must be an .ogg path, got {rel!r}")
    elif not (ASSETS / rel).is_file():
        rep.error(f"{where}: audio file not found: assets/{rel}")


def check_drawable(rep, where, name):
    if not isinstance(name, str) or not ID.match(name):
        rep.error(f"{where}: bad drawable name {name!r}")
        return
    if not any((DRAWABLE / f"{name}.{ext}").is_file() for ext in ("xml", "png", "webp")):
        rep.error(f"{where}: drawable not found: res/drawable/{name}.(xml|png|webp)")


def check_ids(rep, label, items):
    ids = [i.get("id") for i in items]
    for i in ids:
        if not isinstance(i, str) or not ID.match(i):
            rep.error(f"{label}: bad id {i!r}")
    for i, n in Counter(ids).items():
        if n > 1:
            rep.error(f"{label}: duplicate id {i!r}")
    return set(i for i in ids if isinstance(i, str))


def validate(lenient=False) -> Report:
    rep = Report()
    words_doc = load(rep, "words.json")
    phrases_doc = load(rep, "phrases.json")
    scenes_doc = load(rep, "scenes.json")
    games_doc = load(rep, "games.json", required=False)
    songs_doc = load(rep, "songs.json", required=False)
    if not (words_doc and phrases_doc and scenes_doc):
        return rep

    scenes = scenes_doc.get("scenes", [])
    scene_ids = check_ids(rep, "scenes.json", scenes)
    words = words_doc.get("words", [])
    word_ids = check_ids(rep, "words.json", words)
    word_by_id = {w.get("id"): w for w in words}
    phrase_ids = check_ids(rep, "phrases.json", phrases_doc.get("phrases", []))
    uses: dict[str, list[str]] = defaultdict(list)

    # words
    for w in words:
        where = f"word {w.get('id')!r}"
        for field, kind in (("id", str), ("text", str), ("scene", str), ("tier", int), ("audio", str), ("image", str), ("tags", list), ("translations", dict)):
            if not isinstance(w.get(field), kind):
                rep.error(f"{where}: field {field!r} missing or not {kind.__name__}")
        if w.get("tier") not in (1, 2, 3):
            rep.error(f"{where}: tier must be 1-3, got {w.get('tier')!r}")
        if w.get("scene") not in scene_ids:
            rep.error(f"{where}: unknown scene {w.get('scene')!r}")
        if not str(w.get("text", "")).strip():
            rep.error(f"{where}: empty text")
        check_audio(rep, where, w.get("audio"))
        check_drawable(rep, where, w.get("image"))
        if not w.get("tags") or not all(isinstance(t, str) and ID.match(t) for t in w.get("tags", [])):
            rep.error(f"{where}: tags must be a non-empty list of ids")
        for loc, txt in (w.get("translations") or {}).items():
            if not LOCALE.match(loc) or not isinstance(txt, str) or not txt.strip():
                rep.error(f"{where}: bad translation {loc!r}: {txt!r}")

    for p in phrases_doc.get("phrases", []):
        if not str(p.get("text", "")).strip():
            rep.error(f"phrase {p.get('id')!r}: empty text")
        check_audio(rep, f"phrase {p.get('id')!r}", p.get("audio"))

    # scenes
    for s in scenes:
        where = f"scene {s.get('id')!r}"
        if s.get("icon") not in word_ids:
            rep.error(f"{where}: icon {s.get('icon')!r} is not a word")
        if s.get("phrase") not in phrase_ids:
            rep.error(f"{where}: phrase {s.get('phrase')!r} not in phrases.json")
        if s.get("tint") is None:
            rep.error(f"{where}: missing tint")
        check_drawable(rep, where, s.get("background"))
        members = [w for w in words if w.get("scene") == s.get("id")]
        tier1 = sum(1 for w in members if w.get("tier") == 1)
        if tier1 < MIN_TIER1_PER_SCENE:
            rep.error(f"{where}: needs at least {MIN_TIER1_PER_SCENE} tier-1 words, has {tier1}")
        layout_path = CONTENT / str(s.get("layout", ""))
        if not s.get("layout") or not layout_path.is_file():
            rep.warn(f"{where}: no layout file yet ({s.get('layout')})")
            continue
        try:
            layout = json.loads(layout_path.read_text(encoding="utf-8"))
        except json.JSONDecodeError as e:
            rep.error(f"{where}: layout invalid JSON ({e})")
            continue
        if layout.get("scene") != s.get("id"):
            rep.error(f"{where}: layout 'scene' must be {s.get('id')!r}")
        for wid, pl in (layout.get("placements") or {}).items():
            pw = f"{where} placement {wid!r}"
            if wid not in word_ids:
                rep.error(f"{pw}: not a word")
                continue
            if word_by_id[wid].get("scene") != s.get("id"):
                rep.error(f"{pw}: word belongs to scene {word_by_id[wid].get('scene')!r}")
            for axis in ("x", "y"):
                if not isinstance(pl.get(axis), (int, float)) or not 0 <= pl[axis] <= 1:
                    rep.error(f"{pw}: {axis} must be 0..1")
            if not isinstance(pl.get("size", 0.16), (int, float)) or not 0.05 <= pl.get("size", 0.16) <= 0.4:
                rep.error(f"{pw}: size must be 0.05..0.4")
        # every word of the scene appears in it (placed or auto-laid-out)
        for w in members:
            uses[w["id"]].append(f"scene:{s['id']}")

    # games
    if games_doc:
        games = games_doc.get("games", [])
        check_ids(rep, "games.json", games)
        for g in games:
            where = f"game {g.get('id')!r}"
            if g.get("type") not in GAME_TYPES:
                rep.error(f"{where}: type must be one of {sorted(GAME_TYPES)}")
            if g.get("phrase") not in phrase_ids:
                rep.error(f"{where}: phrase {g.get('phrase')!r} not in phrases.json")
            decks = g.get("decks") or {}
            if not decks:
                rep.error(f"{where}: no decks")
            for deck_id, deck in decks.items():
                dw = f"{where} deck {deck_id!r}"
                ws = deck.get("words", [])
                for wid in ws:
                    if wid not in word_ids:
                        rep.error(f"{dw}: unknown word {wid!r}")
                for tier in (1, 2, 3):
                    n = sum(1 for wid in ws if wid in word_by_id and word_by_id[wid]["tier"] <= tier)
                    need = g.get("minWordsPerTier", 2)
                    if n < need:
                        rep.error(f"{dw}: tier {tier} has {n} words, needs {need}")
                for wid in set(ws):
                    uses[wid].append(f"game:{g.get('id')}")

    # songs
    if songs_doc:
        songs = songs_doc.get("songs", [])
        check_ids(rep, "songs.json", songs)
        for song in songs:
            where = f"song {song.get('id')!r}"
            if not str(song.get("title", "")).strip():
                rep.error(f"{where}: missing title")
            check_audio(rep, where, song.get("audio"))
            tokens = [t for line in song.get("lines", []) for t in line.get("tokens", [])]
            if not tokens:
                rep.error(f"{where}: no lyrics")
            song_words = set()
            for t in tokens:
                if "word" in t:
                    if t["word"] not in word_ids:
                        rep.error(f"{where}: unknown word {t['word']!r}")
                    else:
                        song_words.add(t["word"])
            timings = song.get("timings")
            if not isinstance(timings, list) or len(timings) != len(tokens):
                rep.error(f"{where}: timings missing or out of date (run scripts/gen_audio.py)")
            for wid in song_words:
                uses[wid].append(f"song:{song.get('id')}")

    # usage rule
    for wid in sorted(word_ids):
        n = len(uses[wid])
        if n < MIN_USES:
            msg = f"word {wid!r}: used in {n} place(s) {uses[wid]}, needs {MIN_USES}"
            rep.warn(msg) if lenient else rep.error(msg)

    rep.summary = (
        f"{len(words)} words in {len(scenes)} scenes, {len(phrase_ids)} phrases, "
        f"{len((games_doc or {}).get('games', []))} games, {len((songs_doc or {}).get('songs', []))} songs; "
        f"{sum(1 for w in word_ids if len(uses[w]) >= MIN_USES)} of {len(word_ids)} words used in {MIN_USES}+ places"
    )
    return rep


def main():
    rep = validate(lenient="--lenient" in sys.argv)
    for w in rep.warnings:
        print("warning:", w)
    for e in rep.errors:
        print("ERROR:", e)
    print(getattr(rep, "summary", ""))
    if rep.errors:
        print(f"FAILED with {len(rep.errors)} error(s)")
        return 1
    print("OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
