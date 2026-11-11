# Content

All content is data in `app/src/main/assets/content/`. The UI never hard-codes a word.
Run `python scripts/validate_content.py` after every change.

## Files
| File | What it holds |
|---|---|
| `words.json` | the 100 words |
| `phrases.json` | every spoken line used by the mascot and the UI |
| `scenes.json` | the scene list shown on the home screen |
| `scenes/<scene>.json` | where words sit in a scene (optional placements) |
| `games.json` | the four mini-games and their word decks |
| `songs.json` | songs, lyrics and word timings (timings are written by `gen_audio.py`) |

## Word schema
```json
{
  "id": "cow",                       // lowercase, unique, also the file name stem
  "text": "cow",                     // what is spoken and shown to parents
  "scene": "farm",                   // a scene id from scenes.json
  "tier": 1,                         // 1 = first words (short, concrete), 2 = next, 3 = stretch
  "audio": "audio/words/cow.ogg",    // path under assets/
  "image": "img_cow",                // drawable name under res/drawable/
  "tags": ["animal"],                // used by games (feed uses "food")
  "translations": {}                 // locale -> gloss, e.g. {"ur": "..."}; empty for now
}
```

## Adding a word
1. Add the entry to `words.json`.
2. Run `python scripts/gen_audio.py` (audio) and `python scripts/gen_art.py` (placeholder art; a hand-made
   `res/drawable/img_<id>.xml` or `.png` replaces it).
3. Reference it in at least one game deck or song so it is used in 2 places.
4. Run `python scripts/validate_content.py`.

The word appears in its scene automatically. If the scene layout has no placement for it, the renderer puts it
in the next free slot of the scene grid.

## Swapping assets
- Audio: replace `assets/audio/<kind>/<id>.ogg` (mono Ogg Vorbis, normalised). Or change the voice in
  `scripts/gen_audio.py` and run it with `--force`.
- Art: replace `res/drawable/img_<id>.xml` (or add a `.png`/`.webp` and delete the `.xml`).
  `gen_art.py` never overwrites a file that lacks its GENERATED marker.
