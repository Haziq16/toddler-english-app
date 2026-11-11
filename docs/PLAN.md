# Plan

Status values: `todo`, `in-progress`, `done`, `blocked`.

| Session | Status |
|---|---|
| S0 Foundations | done |
| S1 Content and look | done |
| S2 Shell | done |
| S3 Scene engine + Farm scene | todo |
| S4 Mascot and rewards | todo |
| S5 Mini-games and parent corner | todo |
| S6 Remaining scenes and songs | todo |
| S7 Polish and compliance | todo |
| S8 Release prep | todo |

## S0 Foundations
- **Goal:** a buildable, testable, linted project with CI and handoff files.
- **Deliverables:** git repo, Gradle project, Compose theme, ktlint, JUnit runner, GitHub Actions CI, AGENTS.md, CLAUDE.md, docs/PLAN.md, docs/DECISIONS.md, docs/LOG.md.
- **Done when:** `./gradlew assembleDebug testDebugUnitTest` passes and the commands are written into DECISIONS.md.

## S1 Content and look
- **Goal:** the content model and the visual language.
- **Deliverables:** word schema, `scripts/validate_content.py`, 100 words across 5 scenes (home, farm, food, bedtime, outdoors; about 20 each), design tokens (colour, type, spacing, 64dp touch sizes), mascot design, placeholder audio via `scripts/gen_audio.py`.
- **Done when:** the validator passes and the tokens exist.

## S2 Shell
- **Goal:** a child-safe app frame.
- **Deliverables:** home screen with big scene icons, navigation, parental gate (press-and-hold plus an adult-level question), back handling so a toddler cannot exit by accident, settings stub, audio service.
- **Done when:** the gate blocks a random-tap sequence and audio plays with no network.

## S3 Scene engine + Farm scene
- **Goal:** scenes are pure data.
- **Deliverables:** data-driven renderer, hit-testing, word audio, highlight animation, Farm scene.
- **Done when:** adding a word to the JSON makes it appear with no code change.

## S4 Mascot and rewards
- **Goal:** warmth and encouragement.
- **Deliverables:** mascot idle / celebrate / encourage states, sticker collection, a positive response to every tap.
- **Done when:** no screen has a wrong-answer state.

## S5 Mini-games and parent corner
- **Goal:** four games and the parent tools.
- **Deliverables:** pop bubbles, matching, sing-along, feed the mascot, all reading content at tiers 1 to 3. Parent corner with progress, session timer (auto-pause at about 10 minutes, resume via gate) and tier selection.
- **Done when:** each game works at all three tiers.

## S6 Remaining scenes and songs
- **Goal:** full content.
- **Deliverables:** 4 more scenes, 5 to 8 songs with synced word highlight.
- **Done when:** the validator confirms 100 words, each used in 2 or more places.

## S7 Polish and compliance
- **Goal:** accessible, robust and policy-ready.
- **Deliverables:** TalkBack labels, contrast, touch-target audit, airplane-mode test, low-end performance pass, docs/COMPLIANCE.md (Families policy, target audience and content, Data safety, IARC notes, privacy policy text for GitHub Pages).
- **Done when:** every item is ticked or logged as a decision.

## S8 Release prep
- **Goal:** everything needed to upload, except the owner-only steps.
- **Deliverables:** signed AAB steps in docs/RELEASE.md, store listing text, icon and feature-graphic specs, screenshot list, tester guide.
- **Done when:** the docs are complete. Note: a new personal Play developer account needs a closed test with at least 12 testers opted in for 14 consecutive days before production access, so tester recruiting should start as soon as S7 is done.

## Backlog
- (empty)
