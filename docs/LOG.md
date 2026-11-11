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
