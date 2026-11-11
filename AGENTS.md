# AGENTS.md: rulebook for every agent

This is the single rulebook for Claude Code, Codex and any other agent working on this repository.
`CLAUDE.md` only points here.

## Project summary
Toddler English is a free, offline Android app that teaches English words to children aged 1 to 5.
Children tap things in illustrated scenes to hear words, sing along to short songs, and play four
mini-games (pop bubbles, matching, sing-along, feed the mascot). A parent corner behind a parental
gate shows progress, sets a play-time limit and chooses the word tier. It is built for a real
Google Play release in the Designed for Families programme.

- Native Android, Kotlin, Jetpack Compose, single module `app`, package `app.toddlerenglish`.
- Free. No ads, no purchases, no analytics, no third-party SDKs that phone home.
- No `INTERNET` permission. The manifest removes it explicitly and a unit test checks the merged manifest.
- Offline-first: all audio and art are bundled. Progress and settings live in DataStore. No accounts, no backend.
- Content is data in `app/src/main/assets/content/`, never hard-coded in the UI.

## Child rules (never break these)
1. No failure states. Nothing a child does is ever "wrong" on screen.
2. No wrong-answer sounds. A non-target tap names the thing tapped and the mascot encourages.
3. No timers, countdowns or streaks aimed at the child. The play-time limit is a parent setting and its
   pause screen is calm, with no countdown shown.
4. Every touch target is at least 64dp (`Dimens.TouchTarget`).
5. Every word has audio. The validator fails if a word has no audio file.
6. Parent-only areas sit behind the parental gate (press-and-hold, then an adult-level question).
7. Every tap gets a positive response (sound, movement or both).

## Read first, in this order
1. The last 3 entries of `docs/LOG.md`
2. `docs/PLAN.md`
3. `docs/DECISIONS.md`

## Session protocol
**Start**
- Read the files above. Run `git status` and `git log --oneline -5`.
- Pick the first session in `docs/PLAN.md` that is not `done`. Mark it `in-progress` and commit that change.
- Do the "Next up" action from the newest LOG entry first.

**During**
- Stay inside the current session's scope. New ideas go to the Backlog section of `docs/PLAN.md`.
- Small commits. Never rewrite working code for style.
- Record every new decision in `docs/DECISIONS.md` (status `proposed` unless the owner accepted it).
- Add every font, sound or image source to `docs/CREDITS.md`.

**End (mandatory, even if cut short)**
1. Run the build and tests (commands below). They pass, or the failure is written in the LOG entry.
2. Append a LOG entry using the template below.
3. Update the session status in `docs/PLAN.md`.
4. Commit with the message `S<n>: <summary>` and push if a remote exists.

## Commands
```
./gradlew assembleDebug testDebugUnitTest lintDebug ktlintCheck
python scripts/validate_content.py
python scripts/gen_audio.py        # regenerates all placeholder audio (Windows voice)
python scripts/gen_art.py          # regenerates word vector drawables
```
On Windows use `gradlew.bat`.

## LOG entry template
```
## YYYY-MM-DD HH:MM, <agent> (<model>), S<n>
**Done:** what was finished
**Files:** main files added or changed
**State:** does it build, do tests pass, what runs
**Decisions:** D-numbers added or changed
**Open:** problems, blockers, unverified things
**Next up:** the exact first action for whoever resumes
```

## Collaboration rules
- One writer at a time. Before starting, check the newest LOG entry and `git log` for another agent's
  unfinished session. If one is `in-progress` and not yours, do not write; log it and stop.
- Never silently diverge from an accepted decision. To change one, add a new decision that supersedes it.
- Accepted decisions are never edited, only superseded. Proposed ones may be edited until accepted.
- `docs/LOG.md` is append-only.
- Never commit a keystore, signing passwords or API keys.
