# FEAT-016 — Dependency Version Updates

## Objective

Bring existing Gradle dependencies and the AGP/Gradle toolchain up to their latest stable versions before Play Store submission. Update-only — no new libraries introduced, no dependency removed.

## Scope

In scope: version bumps to libraries already declared in `gradle/libs.versions.toml`, the AGP plugin, and the Gradle wrapper, where the bump is a stable-to-stable version change with no required application-code migration.

Out of scope (deferred — see checklist item 6 in project memory): Play Billing Library 8.0.0 → 9.1.0. Google's PBL9 migration guide documents real API-surface changes (not a drop-in bump), so it needs its own spec/plan touching `features/monetization` purchase-flow code. Decision confirmed with user on 2026-10-20.

## Target Versions

All versions below were verified directly against Google Maven / Maven Central `maven-metadata.xml` (not search results — an earlier research pass using web search got the Gradle and AGP targets wrong; corrected here).

| Dependency | Current | Target | Notes |
|---|---|---|---|
| Gradle wrapper | 9.4.1 | 9.8.2 | Actual current stable per `services.gradle.org/versions/current`. Bundles Kotlin 2.4.10 for build scripts (app module's Kotlin compiler comes from AGP, verified separately via a successful build). |
| AGP (`com.android.application`) | 9.2.1 | 9.4.1 | Actual latest stable per Google Maven metadata (9.5.0 still alpha-only) |
| `compose-compiler` plugin | 2.3.0 | 2.3.0 (unchanged) | Confirmed compatible — `assembleDebug`/`assembleRelease` both succeeded with AGP 9.4.1 + Gradle 9.8.2 at this version |
| `androidx.core:core-ktx` | 1.18.0 | 1.19.1 | |
| `androidx.appcompat:appcompat` | 1.7.1 | 1.8.0 | |
| `com.google.android.material:material` | 1.12.0 | 1.14.0 | |
| `androidx.compose:compose-bom` | 2025.04.01 | 2026.09.00 | BOM bump, low risk by design |
| `com.google.devtools.ksp` | 2.3.7 | 2.3.12 | Standalone versioning; verify it still pairs with the Kotlin version AGP 9.4.1 bundles |
| `com.google.dagger:hilt-android` (+compiler) | 2.59.2 | 2.60.1 | |
| `androidx.hilt:hilt-navigation-compose` | 1.2.0 | 1.4.0 | |
| `androidx.room:room-runtime` (+ktx, +compiler) | 2.7.1 | 2.8.5 | Check schema-export compatibility |
| `androidx.navigation:navigation-compose` | 2.9.0 | 2.10.2 | |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android`/`-test` | 1.10.1 | 1.11.0 | |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.9.0 | 2.11.0 | |
| `androidx.core:core-splashscreen` | 1.0.1 | 1.2.0 | |
| `androidx.activity:activity-compose` | 1.10.1 | 1.13.0 | |
| `androidx.test.espresso:espresso-core` | 3.6.1 | 3.7.0 | |
| `com.google.android.gms:play-services-ads` | 24.4.0 | 25.5.0 | Major; verify `InterstitialAdLoadCallback`/`FullScreenContentCallback` call sites still compile |

No change needed (already latest stable, verified 2026-10-20): `androidx.arch.core:core-testing` (2.2.0), `androidx.test.ext:junit` (1.3.0), `junit` (4.13.2).

## Risk Areas

- **AGP 9.2.1 → 9.3.2**: AGP 9.x bundles Kotlin compilation directly (per CLAUDE.md build notes — no separate `org.jetbrains.kotlin.android` plugin). A minor AGP bump can shift the bundled Kotlin version, which may require a matching `compose-compiler` plugin version bump. Verify by running a clean build; if it fails on a compose-compiler mismatch, bump that plugin version to match.
- **Room 2.7.1 → 2.8.5**: schema location is exported to `$projectDir/schemas` (see `ksp { arg("room.schemaLocation", ...) }`). Confirm no schema-export format change breaks the KSP step.
- **play-services-ads 24.4.0 → 25.5.0**: major version jump. Our usage surface is narrow (`AdMobDataSource`: `InterstitialAd.load`, `InterstitialAdLoadCallback`, `FullScreenContentCallback`, `AdRequest.Builder`). Confirm these are unchanged after bump; this project's `minSdk` (29) already exceeds the SDK's own minimum.

## Verification

- `./gradlew assembleDebug` and `./gradlew assembleRelease` both compile clean.
- `./gradlew test` — existing unit test suite passes unchanged (no application logic is touched by this feature).
- `./gradlew lint` — no new findings introduced by the bump.
