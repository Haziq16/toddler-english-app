# Release guide

How to build a signed Android App Bundle (AAB) and get it onto Google Play. Steps marked **Owner** need the
owner's own accounts or secrets and must never be done by an agent.

## 0. Before every release
1. All checks green:
   ```
   python scripts/validate_content.py
   ./gradlew ktlintCheck lintDebug testDebugUnitTest assembleDebug
   ./gradlew connectedDebugAndroidTest      # with an emulator or phone attached
   ```
2. Bump the version in `app/build.gradle.kts`: `versionCode` +1 every upload (Play rejects reused codes),
   `versionName` as people should see it (for example `1.0.0`).
3. Check `targetSdk` still meets Play's rule (36 from 31 Aug 2026; see D-008).
4. Read `docs/COMPLIANCE.md` section 4 and close any open owner items.

## 1. Create the upload key (Owner, once)
Play App Signing holds the real app signing key. You keep only an **upload key**.
```
keytool -genkeypair -v -keystore toddler-english-upload.jks -alias upload -keyalg RSA -keysize 4096 -validity 10000
```
- Keep `toddler-english-upload.jks` **outside the repository** (for example in a password manager vault plus an
  offline backup). `.gitignore` already ignores `*.jks`, `*.keystore` and `keystore.properties`.
- If the upload key is ever lost, Play support can reset it; the app signing key is safe with Google.

## 2. Point the build at the key (Owner, on the build machine)
Create `keystore.properties` in the repository root (ignored by git):
```
storeFile=C:/secure/toddler-english-upload.jks
storePassword=...
keyAlias=upload
keyPassword=...
```
`app/build.gradle.kts` signs the `release` build with it when the file exists. Without the file the bundle is
built unsigned (useful for CI checks only).

## 3. Build the bundle
```
./gradlew clean bundleRelease
```
Output: `app/build/outputs/bundle/release/app-release.aab` (about 6 MB). The build fails if the merged
manifest asks for INTERNET, ACCESS_NETWORK_STATE or AD_ID.

Check it before upload:
```
jarsigner -verify -verbose app/build/outputs/bundle/release/app-release.aab | tail -3
```
Optional local install test of the exact bundle: use `bundletool build-apks --connected-device` with the same
keystore, then `bundletool install-apks`.

For on-device performance checks there is a `benchmark` build type (`./gradlew assembleBenchmark`): release code
signed with the debug key. Never upload it.

## 4. Play Console setup (Owner, once)
1. Create the developer account (personal or organisation). A **new personal account** must run a closed test
   with **at least 12 testers opted in for 14 days in a row** before it can apply for production access.
2. Create the app: name "Toddler English" (or the final name), default language English (United States),
   App, Free.
3. App content: fill every declaration using `docs/COMPLIANCE.md` section 3 (privacy policy URL, ads, app
   access, content rating, target audience **5 and under**, data safety, government/financial/health: no).
4. Store listing: copy from `docs/STORE_LISTING.md`; upload icon, feature graphic and screenshots.
5. Turn on Play App Signing (default for new apps) when you upload the first bundle.

## 5. Testing tracks
1. **Internal testing** (up to 100 people, no review delay): upload the first AAB here, install it from the
   opt-in link on two or three of your own devices, and walk through `docs/TESTER_GUIDE.md` yourself.
2. **Closed testing**: create a track, add testers by email list or Google Group, share the opt-in link and
   `docs/TESTER_GUIDE.md`. Keep **12 or more testers opted in for 14 consecutive days**. Recruit about 15-20 in case
   some drop out; a tester who opts out breaks their own streak, not the count of others.
3. After 14 days, apply for production access in the Play Console (it asks about the test and what you changed).
4. **Production**: create a release from the same or a newer AAB, roll out to 20%, then 100%.

## 6. After release
- Watch Android vitals (crashes, ANRs) in the Play Console; there is no crash SDK in the app by design.
- Every update: new `versionCode`, run section 0 again, upload to internal testing first.
