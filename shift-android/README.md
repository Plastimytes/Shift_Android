# SHIFT (Android) — offline automotive learning app

**Status: early alpha source. NOT compiled, NOT run, NO APK produced.**
The authoring environment had Java 21 but no Gradle, Kotlin compiler, Android SDK or network, so nothing here has been built.
What *was* checked: Kotlin brace/paren balance, that every `app.shift.learn` import points at a declared symbol, XML and JSON well-formedness, and the bundled lesson against `tools/validate_content.py`. Unit tests are written but have **not been run**.

## Offline by design
- The manifest requests **no permissions** (no INTERNET). Adding one must be a documented decision.
- All content is bundled in `app/src/main/assets/content/` (lessons, categories, curriculum, source registry).
- Preferences use DataStore; progress, quiz attempts and bookmarks use Room. Everything stays on the device. `allowBackup` is off.
- Fonts: the system font is used. Inter (OFL) is **not** bundled yet. Add `res/font/` files and confirm licences rather than using a downloadable-font provider, which needs the network.

## Build (in a networked environment with Android Studio or a JDK 17+ and the Android SDK)
1. Open the folder in Android Studio, or run `gradle wrapper --gradle-version 8.9` once to create the wrapper (the jar is not included).
2. **Update `gradle/libs.versions.toml` to current stable versions.** The pinned versions are from my own knowledge and were not checked. Re-check `targetSdk` against Google Play's current requirement (35 was assumed).
3. `./gradlew testDebugUnitTest` then `./gradlew assembleDebug`. The APK appears in `app/build/outputs/apk/debug/`.
4. Release: create `keystore.properties` (never commit it) with `storeFile`, `storePassword`, `keyAlias`, `keyPassword`, then `./gradlew assembleRelease` (or `bundleRelease` for Play). Enable minification only after adding keep rules for kotlinx.serialization and Room.

## What is implemented
- Onboarding: search, group filter, selection capped at exactly 3, Done disabled until 3 (`domain/InterestSelection.kt`), 133 interest categories from the blueprint.
- Home: mode switch (Easy/Hard/Performance/Top Formula), chosen interests, Continue card, lesson list, bookmarks.
- Three section screens listing all 55 planned modules, labelled **Planned** unless lessons exist.
- Lesson reader: renders text, specification tables (each number shows its source and locator), safety and stop-and-refer blocks, key terms, misconceptions, sources panel with tier and confidence, mode-filtered blocks.
- Quiz: multiple-choice, multi-select, true/false, calculation; shuffled order; explanations and sources after submit; 80% pass mark, strict-safety lessons use their own threshold (100% for the sample); results saved to Room.
- **One** lesson: `L-ABT-08-03` Brake fluid (status: draft, open issues listed in-app).

## Not implemented (do not describe the app as having these)
Search across lessons, glossary, virtual garage, engine simulator, diagnostic simulator, calculators, workshop/build planner, modification planner, vehicle-specific filters, jurisdiction selector, spaced repetition, achievements/XP, settings screen, light/dark toggle (follows system), diagram and interactive blocks (shown as "not included"), image assets, remote content updates, cloud sync, migrations (database is version 1), instrumented/UI tests, accessibility audit, hazard-stripe styling on safety blocks.

## Decisions
- Single Gradle module for now; split into feature modules when content and screens grow.
- Manual DI (`AppContainer`) instead of Hilt to keep the first slice small.
- Content is JSON validated by schema rules in `tools/validate_content.py`; `tools/export_assets.py` regenerates catalogue files from `docs/*.xlsx`.
- Application ID `app.shift.learn` is provisional until trademark, domain and store checks.

## Repo map
`app/` Android module · `content-src/` schema and lesson sources · `tools/` validators and exporters · `docs/` source registry and curriculum blueprint workbooks.

## Building in GitHub Codespaces (untested)
1. Push this folder to a GitHub repo, then Code > Codespaces > Create. `.devcontainer/` installs JDK 17 and runs `scripts/setup-android.sh` (Android SDK 35, Gradle 8.9 wrapper).
2. In the terminal: `./gradlew testDebugUnitTest assembleDebug`. Fix compile errors, rerun.
3. Download `app/build/outputs/apk/debug/app-debug.apk` (right-click in the file explorer > Download) and install it on a phone.
Codespaces usually cannot run the Android emulator, so test on a real device. Alternatively, push and let `.github/workflows/android.yml` build and upload the APK as an artifact.
The setup script's SDK download filename and the action versions come from memory and may need updating.
