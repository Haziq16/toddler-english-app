# Architecture

Single module `app`, package `app.toddlerenglish`.

| Package | What lives there |
|---|---|
| `content` | JSON models (`Models.kt`) and `ContentLoader`. Pure Kotlin, unit-tested against the real files. |
| `audio` | `AudioService` (SoundPool, words and phrases, back-to-back sequences), `SongPlayer` (Media3), `AudioIndex`. |
| `data` | DataStore repositories: `SettingsRepository`, `ProgressRepository`. |
| `parent` | Parental gate (logic + screen), parent corner, session timer. |
| `games` | The four mini-games and their pure round logic. |
| `ui` | Navigation (`AppNavHost`), screens, components (Pebble, buttons), scene engine, theme tokens. |

`AppContainer` (created by `ToddlerApplication`) is the hand-made dependency container.

## Scene engine
`ui/scene/SceneLayoutEngine` turns `words.json` + `scenes/<id>.json` into positions. Words with a placement go
there; any other word of that scene goes to the first free auto slot. Only words up to the parent's tier show.
`SceneScreen` draws the background drawable stretched to the screen (backgrounds are bands and hills only, so
stretching looks fine), then each word's drawable at its position. One tap handler runs `hitTest`, which picks
the nearest item within 1.25 x its radius (and never less than a 64dp target). A tap on empty space makes a
sparkle and Pebble nods, so every tap gets a positive response.

## Audio
Word and phrase clips are short Ogg files played by SoundPool from assets (`noCompress` keeps them
memory-mappable). `assets/audio/index.json` holds clip lengths so phrases like "Can you find the" + "cow" play
back to back. Songs are longer files played by Media3 ExoPlayer; the current position drives word highlighting.
