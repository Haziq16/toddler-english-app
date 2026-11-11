# Compliance and quality (S7)

Status: ✅ done and verified · 📝 decision logged · ⏳ owner action needed.
Everything here was checked on 12 Nov 2026 against the code at the S7 commit.

## 1. Accessibility and quality audit

| Item | Status | How it is checked |
|---|---|---|
| TalkBack labels on everything a child can tap | ✅ | `ChildScreensAuditTest` (device test) walks home, all 5 scenes, games menu, Bubbles, Find it, Feed Pebble, songs list, sticker book and the gate, and fails if any clickable node has no label. `bouncyClickable` gives each button one spoken label. Scene items have a label and a "Hear <word>" click action. |
| Touch targets at least 64dp | ✅ | Same device test checks the real size of every clickable node. `Dimens.TouchTarget = 64.dp` is enforced by `bouncyClickable` and scene items are never drawn smaller. |
| Text contrast (WCAG AA 4.5:1) | ✅ | `ContrastTest` (unit test) covers body text, tile titles, buttons, word labels and lyrics. Parent controls use `Palette.Forest`, at least 3:1 against white. |
| Airplane mode | ✅ | On the emulator with airplane mode on: app started, Pebble greeted, a farm word played (SoundPool) and the Good Night song played (Media3). No network errors in logcat. The app also holds no network permission (build check `check<Variant>Permissions`). |
| Low-end performance | ✅ 📝 | R8 release build is 3.5 MB. In Bubbles the app's own work per frame is about 0.5 ms (median, from `gfxinfo framestats`) after moving all per-frame reads into layout and draw (D-026). The emulator renders with SwiftShader (software), so its total frame times (about 50 ms) say nothing about real devices. A run on a real low-end phone is still to do (see 4). |
| Tablet and landscape | ✅ | Checked at 1280 x 800 dp landscape: home grid, farm scene and Find it lay out correctly. In landscape some scene items sit close together; the forgiving hit-test picks the nearest. |
| No failure states, no wrong-answer sounds, no timers or streaks for the child | ✅ | `ChildRulesTest` (unit) scans spoken phrases and child-screen code. Game rules (`GameLogicTest`) prove a non-target tap never ends or fails a round. |
| Every word has audio | ✅ | `validate_content.py` and `ContentLoaderTest`. |
| Parent-only areas behind a gate | ✅ | Settings, progress, close app and reset are only reachable through `ParentalGate` (hold 3 s + written-out sum). `ParentalGateTest` simulates 20,000 random toddler sessions. |

## 2. Google Play Families policy

The app targets **only** children (ages 5 and under), so the full Families policy applies. How it complies:

| Requirement | How this app meets it |
|---|---|
| Content appropriate for children | Original, gentle pictures and words; no violence, fear, or user-generated content. |
| Ads | None. No ads SDK, no `AD_ID` permission (removed in the manifest). |
| Data collection from children | None. No personal or device identifiers, no analytics, no crash reporting SDK, no network access. |
| SDKs | Only AndroidX, Jetpack Compose, Media3 and kotlinx libraries; none sends data. No SDK appears on Play's list of SDKs not allowed in children's apps. |
| Permissions | No dangerous permissions. Merged manifest requests nothing but the internal `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` that AndroidX adds. |
| Location, camera, microphone, contacts | Not used. |
| Purchases | None. |
| Links out of the app | None. |
| Adult areas | Parent corner behind a parental gate; it holds only settings and an on-device progress summary. |
| Privacy policy | Required for every app with a children target audience. Text in `docs/privacy-policy.md`, served by GitHub Pages (see 6). |

## 3. Play Console declarations (answers to enter)

**Target audience and content**
- Target age groups: **5 and under** only. Do not tick 6-8 or older.
- Could the app unintentionally appeal to children? Not applicable (it is for children).
- Store listing: describe it as an app for young children learning English; mention the parent gate.

**Ads**: "No, my app does not contain ads."

**App access**: "All functionality is available without special access" (no login).

**Data safety**
- Does your app collect or share any of the required user data types? **No.**
- Reason: Play counts data as "collected" only when it is sent off the device. All settings and play counts stay in the app's private storage and are excluded from cloud backup (`data_extraction_rules.xml`, `backup_rules.xml`).
- Is all data encrypted in transit? Not applicable (no data transmitted).
- Do you provide a way for users to request deletion? Not applicable; parents can also clear progress in the app.
- Independent security review: No.

**Content rating (IARC questionnaire)**
- Category: "All other app types" (reference / education); if offered, "Game" is also acceptable for the mini-games.
- Violence, fear, sexuality, gambling, drugs, alcohol, tobacco, crude humour, bad language: **No** to all.
- Users can interact or exchange content: **No**. Shares location: **No**. Digital purchases: **No**.
  Unrestricted internet: **No**.
- Expected result: ESRB Everyone, PEGI 3, USK 0, IARC 3+.

**Government, financial, health, news, COVID-19 apps**: No to all.

**Teacher Approved** (optional, later): the app fits the programme's criteria (no ads, no purchases, age-appropriate); apply after launch.

## 4. Open items for the owner

| Item | Status |
|---|---|
| Fill `[developer name]` and `[developer contact email]` in `docs/privacy-policy.md` (Play requires a contact). | ⏳ |
| Confirm or replace the placeholder voice before release (D-013). | ⏳ |
| Run the app once on a real low-end phone (2 GB RAM, Android 7-9) and play Bubbles for a minute. | ⏳ |
| Confirm the application id `app.toddlerenglish` (cannot change after first upload, D-009). | ⏳ |

## 5. Decisions made in S7
D-026 (per-frame reads outside composition, benchmark build type), D-027 (privacy policy hosting).

## 6. Privacy policy hosting
GitHub Pages serves the `docs/` folder of `main`. The policy URL is
`https://haziq16.github.io/toddler-english-app/privacy-policy`. Use it in the Play Console
(App content > Privacy policy) once the placeholders are filled.
