# Log

Append-only. Newest entry at the bottom. Template is in AGENTS.md.

## 2026-11-12 09:40, Claude Code (Claude Opus 5.5), S0
**Done:** handoff files, Gradle project (single `app` module), Compose theme and first tokens, ktlint, Android lint,
JUnit runner, CI workflow, permission guard task, launcher icon with the mascot, placeholder validator.
**Files:** AGENTS.md, CLAUDE.md, docs/*, settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml,
app/build.gradle.kts, app/src/main/AndroidManifest.xml, ui/theme/*, MainActivity.kt, .github/workflows/ci.yml
**State:** `./gradlew assembleDebug testDebugUnitTest lintDebug ktlintCheck` passes locally. Lint: no issues.
Merged manifest has no INTERNET permission.
**Decisions:** D-006 to D-011 added (proposed).
**Open:** CI not yet observed on GitHub.
**Next up:** start S1: write `app/src/main/assets/content/words.json` and the real `scripts/validate_content.py`.

## 2026-11-12 10:30, Claude Code (Claude Opus 5.5), S1
**Done:** content schema and Kotlin models/loader, 100 words in 5 scenes (tiers 1-3), 27 spoken phrases,
scene index, design tokens (Palette, Dimens with 64dp TouchTarget, Motion, type scale), mascot Pebble
(design sheet + Canvas drawing with moods), 100 word drawables + 5 scene backgrounds from `gen_art.py`,
127 placeholder audio clips from `gen_audio.py`, real `validate_content.py`, art preview page.
**Files:** assets/content/{words,phrases,scenes}.json, assets/audio/**, res/drawable/img_*.xml, bg_*.xml,
content/Models.kt, content/ContentLoader.kt, ui/components/Pebble.kt, ui/theme/Tokens.kt, scripts/*,
docs/CONTENT.md, docs/MASCOT.md, docs/CREDITS.md
**State:** validator passes (lenient: usage rule is a warning until games and songs exist).
`assembleDebug testDebugUnitTest lintDebug ktlintCheck` pass.
**Decisions:** D-012 to D-016 (proposed). D-013 flags that the Windows voice may not be redistributable.
**Open:** voice licence (D-013). Art checked by eye in the preview only, not yet on a device.
**Next up:** start S2: write `audio/AudioService.kt` (SoundPool for words and phrases) and the navigation shell.

## 2026-11-12 11:20, Claude Code (Claude Opus 5.5), S2
**Done:** app container, AudioService (SoundPool, sequences with durations from `assets/audio/index.json`),
SettingsRepository (DataStore), parental gate logic + screen, parent corner stub (sound switch, close app),
home screen with five big scene tiles and animated Pebble, navigation, Back handling, scene stand-in grid.
**Files:** AppContainer.kt, audio/*, data/SettingsRepository.kt, parent/*, ui/AppNavHost.kt,
ui/screens/HomeScreen.kt, ui/screens/SceneScreen.kt, ui/components/{Common,Mascot,ParentButton}.kt
**State:** build, unit tests (incl. random-tap gate test), lint and ktlint pass. Checked on the Pixel 8 Pro
emulator (API 35): home renders, Back x3 stays in the app, 3 s hold shows the question, right answer opens the
parent corner, SoundPool playback events logged for greeting, gate phrase, scene name and a word. The app holds
no network permission (checked with dumpsys).
**Decisions:** D-017 to D-019 (proposed).
**Open:** airplane-mode run is planned for S7 (network cannot be used anyway without the permission).
**Next up:** start S3: write `ui/scene/SceneLayoutEngine.kt` (placements + auto-slots) and `assets/content/scenes/farm.json`.

## 2026-11-12 12:00, Claude Code (Claude Opus 5.5), S3
**Done:** data-driven scene renderer (`SceneScreen`), pure layout engine with auto slots and forgiving
hit-testing (`SceneLayoutEngine`), highlight animation (grow, wiggle, glow, word label), sparkle on empty taps,
home button, Farm layout (`scenes/farm.json`, 20 placements). Backgrounds redrawn without circles because they
now stretch to the screen.
**Files:** ui/scene/SceneLayoutEngine.kt, ui/screens/SceneScreen.kt, assets/content/scenes/farm.json,
scripts/gen_art.py (backgrounds), res/drawable/ic_home.xml, docs/ARCHITECTURE.md
**State:** build, tests, lint, ktlint pass. `SceneLayoutEngineTest.aWordAddedOnlyToWordsJsonStillAppears`
proves the S3 "done when". Farm checked on the emulator at tier 1: tap on cow plays "Cow", it grows and its
label shows.
**Decisions:** none new.
**Open:** tier 2-3 farm layout not yet seen on screen (tier picker comes in S5). Tablet layout not yet checked.
**Next up:** start S4: write `data/ProgressRepository.kt` (words heard, stickers) and the sticker book screen.

## 2026-11-12 12:45, Claude Code (Claude Opus 5.5), S4
**Done:** Pebble moods animated (idle bob and blink, celebrate hop and wings, encourage tilt, sleep breathing);
ProgressRepository (DataStore, counts only); sticker catalogue (`stickers.json`, 23 stickers) with pure
`Rewards` rules and `Rewarder` coordinator; sticker celebration overlay with spoken "A new sticker for you!";
sticker book screen; `TapSparkles` so empty taps sparkle on home, scene and sticker book; ChildRulesTest.
**Files:** data/{Progress*,Rewards,Rewarder}.kt, ui/components/{Mascot,Stickers}.kt,
ui/screens/StickerBookScreen.kt, ui/AppNavHost.kt, assets/content/stickers.json, validate_content.py
**State:** build, tests, lint, ktlint pass. On the emulator, tapping five farm animals awards the cow sticker.
Wrong-answer audit: no screen has one (only scenes, sticker book, gate and parent corner exist; games in S5
follow the same rule and ChildRulesTest covers the `games` package).
**Decisions:** D-020, D-021 (proposed).
**Open:** none new.
**Next up:** start S5: write `assets/content/games.json` (four games, decks per scene) and `games/GameRounds.kt`.

## 2026-11-12 14:20, Claude Code (Claude Opus 5.5), S5
**Done:** games.json (4 games, decks per scene and a food deck), first song (Farm Friends) with generated timings,
pure game rules (`GameLogic.kt`: tier rules, BubblesRound, FindRound), Bubbles, Find it, Feed Pebble and
Sing-along screens, games menu, Media3 `SongPlayer`, `SessionTimer` and rest screen, full parent corner
(progress by scene, most-heard words, game rounds, word level, play-time limit, sound, close app, reset with
confirmation). Scene items now stay inside the safe area.
**Files:** games/*, audio/SongPlayer.kt, parent/{SessionTimer,ParentCornerScreen}.kt, ui/screens/RestScreen.kt,
ui/AppNavHost.kt, assets/content/{games,songs}.json, assets/audio/songs/farm_friends.ogg
**State:** build, 40 unit tests, lint, ktlint and strict validation pass (all 100 words used in 2+ places).
`GameLogicTest` plays every game at tiers 1-3 on every deck. On the emulator: tier 1 Find it and Bubbles,
tier 3 farm (20 items), Feed Pebble (4 foods), sing-along with synced highlight, parent corner progress.
**Decisions:** D-022, D-023 (proposed).
**Open:** rest screen after a real 5-minute limit not yet watched on device (logic unit-tested).
**Next up:** start S6: write `assets/content/scenes/food.json` placements, then home, bedtime and outdoors.

## 2026-11-12 15:10, Claude Code (Claude Opus 5.5), S6
**Done:** layouts for food, home, bedtime and outdoors (20 placements each); item halos for contrast; six more
original songs (7 total) with generated audio and timings; song stickers (30 stickers, all with distinct art);
lyric-sync and song-count tests. CI now runs the validator in strict mode.
**Files:** assets/content/scenes/{food,home,bedtime,outdoors}.json, assets/content/{songs,stickers}.json,
assets/audio/songs/*.ogg, ui/screens/SceneScreen.kt, ui/theme/Tokens.kt, GameLogicTest.kt, CREDITS.md
**State:** `validate_content.py` (strict): 100 words, each used in 2+ places; 5 scenes, 4 games, 7 songs.
Build, 37 unit tests, lint, ktlint pass. All five scenes checked on the emulator at tier 3.
**Decisions:** D-024, D-025 (proposed).
**Open:** songs are spoken chants, not sung (D-024). Tablet layout still unchecked (S7).
**Next up:** start S7: run the TalkBack/contrast/touch-target audit and write `docs/COMPLIANCE.md`.
