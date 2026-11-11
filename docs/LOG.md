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
