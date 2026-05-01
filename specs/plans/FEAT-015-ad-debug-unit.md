# Plan: FEAT-015 — Debug Ad Unit ID Selection

## Implementation Order

1. **`MonetizationConfig.kt`**:
   - Rename `ADMOB_INTERSTITIAL_UNIT_ID_DEBUG` → `ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS`.
   - Remove stale `// Replace with production values…` comment.
   - Add `fun interstitialUnitId(debug: Boolean = BuildConfig.DEBUG): String`.
2. **`MonetizationConfigTest.kt`** (write first — TDD): two unit tests for the resolver.
3. **`AdMobDataSource.kt`**: replace `MonetizationConfig.ADMOB_INTERSTITIAL_UNIT_ID` with `MonetizationConfig.interstitialUnitId()`.
4. **`specs/README.md`**: add FEAT-015 row.

## Files Modified / Created

| File | Change |
|---|---|
| `features/monetization/MonetizationConfig.kt` | Rename constant; add resolver fn; remove comment |
| `features/monetization/data/AdMobDataSource.kt` | Call `interstitialUnitId()` |
| `test/.../MonetizationConfigTest.kt` | New — 2 unit tests |
| `specs/features/FEAT-015-ad-debug-unit.md` | This spec |
| `specs/plans/FEAT-015-ad-debug-unit.md` | This plan |
| `specs/README.md` | Add FEAT-015 row |

## Verification

```bash
./gradlew :app:testDebugUnitTest   # MonetizationConfigTest passes
./gradlew assembleDebug            # compiles clean
./gradlew assembleRelease          # compiles clean
```

Manual on device (debug build):
1. Trigger an interstitial ad in the app.
2. Verify a test ad appears (labelled "Test Ad" by Google).
