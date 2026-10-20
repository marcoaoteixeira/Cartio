# Plan: FEAT-016 — Dependency Version Updates

## Implementation Order

Each stage is built and tested before moving to the next, so a failure is isolated to a small, known cause.

1. **Gradle wrapper**: bump `gradle/wrapper/gradle-wrapper.properties` distributionUrl to 9.8.2 (actual current stable; initial research target of 9.7.0 was wrong — no such final release exists) + matching sha256 from `services.gradle.org/versions/current`. Run `./gradlew wrapper --gradle-version 9.8.2 --gradle-distribution-sha256-sum <sum>`. Verify: `./gradlew -v`.
2. **AGP**: bump `agp` in `gradle/libs.versions.toml` to 9.3.2. Build. If it fails on a Kotlin/compose-compiler version mismatch, bump `compose-compiler` plugin version in the `[plugins]` block to the version AGP 9.3.2 bundles, then rebuild.
3. **Independent library bumps** (low risk, batch together): `coreKtx`, `composeBom`, `hilt` (+ `hilt-navigation-compose` pinned version), `lifecycleViewModelCompose`, `coreSplashscreen`, `activity-compose` pinned version, `espressoCore`. Build + run unit tests after this batch.
4. **Room**: bump `room` to 2.8.5 alone. Build, run unit tests (DAO/repository tests exercise Room codegen), check `app/schemas/` for a new schema file export.
5. **play-services-ads**: bump `googleAds` to 25.5.0 alone. Build. Confirm `AdMobDataSource.kt` still compiles against `InterstitialAd`, `InterstitialAdLoadCallback`, `FullScreenContentCallback`, `AdRequest.Builder` with no deprecation errors.
6. **Full verification pass**: `assembleDebug`, `assembleRelease`, `test`, `lint`.
7. **`specs/README.md`**: add FEAT-016 row, status Done.

No test-first (TDD) step applies here — this feature changes no application logic or behavior, only dependency coordinates. Existing test suite is the regression gate.

## Files Modified

| File | Change |
|---|---|
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.4.1 → 9.7.0 |
| `gradle/libs.versions.toml` | Version bumps per spec table; `compose-compiler` plugin version only if required by step 2 |
| `app/schemas/*.json` | New Room schema export file, if Room's export format changed (committed as-is, generated) |
| `specs/features/FEAT-016-dependency-updates.md` | This spec |
| `specs/plans/FEAT-016-dependency-updates.md` | This plan |
| `specs/README.md` | Add FEAT-016 row |

## Verification

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew lint
```

All four must pass clean with zero new warnings/errors attributable to the bump before commit.

## Rollback

Each stage is its own commit on the feature branch (not squashed until the end), so a single bad bump can be reverted without redoing the whole chain. If AGP 9.3.2 (step 2) causes a Kotlin/compose-compiler incompatibility that isn't resolved by a compose-compiler bump, stop and flag it to the user rather than pinning to a mismatched combination.
