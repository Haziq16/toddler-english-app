# Decisions

Status is `proposed` or `accepted`. Accepted entries are never edited, only superseded by a new entry.

## D-001 Platform and stack (accepted)
Native Android only, Kotlin + Jetpack Compose, phone-first with tablet-tolerant layouts. Single module `app`.

## D-002 Business model and privacy (accepted)
Free. No ads, no purchases, no analytics, no third-party SDKs that phone home. The `INTERNET` permission is not declared.

## D-003 Offline-first storage (accepted)
All audio and art bundled. Progress and settings stored locally with DataStore. No accounts, no backend.

## D-004 Collaboration (accepted)
Repository on GitHub. Claude Code and Codex share one branch, one writer at a time, everything resumable from files.

## D-005 Child rules (accepted)
No failure states, no wrong-answer sounds, no timers or streaks aimed at the child, touch targets at least 64dp, every word has audio, parent-only areas behind a parental gate.

## D-006 Language (proposed)
English UI and audio. The word schema has a `translations` map keyed by locale (for example `"ur": "..."`) so a first-language gloss can be added later. The parent corner is in English.

## D-007 Project location and relation to TinyTalk (proposed)
This app lives in its own repository and folder, `C:\Users\hp\AndroidStudioProjects\toddler-english-app`.
The folder `MIM` next to it holds a different app (TinyTalk, ages 2 to 6, adventure game) with its own
handover rules and owner decisions. It is left untouched. Nothing is copied from it except the Gradle wrapper
and the dependency versions already proven to build on this machine.

## D-008 SDK levels (proposed)
`minSdk = 24`, `compileSdk = 36`, `targetSdk = 36`. Google Play requires new apps and updates to target
Android 16 (API 36) from 31 Aug 2026. Source: developer.android.com/google/play/requirements/target-sdk,
checked 12 Nov 2026. R8 minification and resource shrinking are on for release.

## D-009 App name and package (proposed)
Display name "Toddler English", application id and package `app.toddlerenglish`. The application id cannot be
changed after the first Play upload, so the owner should confirm it before S8.

## D-010 Theme (proposed)
One calm light palette (tokens in `ui/theme/Tokens.kt`). The app does not follow system dark mode, so scenes
look the same for every child. Text contrast is checked in S7.

## D-011 Build, lint and test commands (proposed)
Toolchain: JDK 17, Gradle 8.13 wrapper, AGP 8.11.2, Kotlin 2.2.21, Compose BOM 2025.10.01.
Format and lint: ktlint through `org.jlleitschuh.gradle.ktlint` 13.1.0 (rules in `.editorconfig`, Android Studio
style with trailing commas allowed) plus Android lint. Tests: JUnit 4 unit tests on the JVM.

    ./gradlew assembleDebug testDebugUnitTest      # S0 "done when"
    ./gradlew ktlintCheck lintDebug                # style and Android lint
    ./gradlew ktlintFormat                         # auto-fix style
    python scripts/validate_content.py             # content checks
    ./gradlew bundleRelease                        # release AAB (needs keystore.properties, see RELEASE.md)

Every `assemble` and `bundle` task also runs `check<Variant>Permissions`, which fails if the merged manifest
contains INTERNET, ACCESS_NETWORK_STATE or AD_ID. CI (`.github/workflows/ci.yml`) runs content validation,
ktlintCheck, lintDebug, testDebugUnitTest and assembleDebug on every push.
