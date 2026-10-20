# Plan: FEAT-018 — Play Billing Library 8 → 9 Migration

## Implementation Order

1. Bump `playBilling` version in `gradle/libs.versions.toml`: `8.0.0` → `9.1.0`.
2. Build: `./gradlew assembleDebug` — confirm no compile errors (expected: none, per spec's API audit).
3. Run `./gradlew test` — confirm `BillingClientWrapperTest` and the rest of the suite pass unchanged.
4. `./gradlew assembleRelease lint` — full verification pass.
5. `specs/README.md`: add FEAT-018 row.

No TDD step needed — no application logic changes, so there's nothing new to drive with a failing test. The existing `BillingClientWrapperTest`/`FakeBillingClientFacade` suite is the regression gate.

## Files Modified

| File | Change |
|---|---|
| `gradle/libs.versions.toml` | `playBilling` 8.0.0 → 9.1.0 |
| `specs/features/FEAT-018-billing-v9-migration.md` | This spec |
| `specs/plans/FEAT-018-billing-v9-migration.md` | This plan |
| `specs/README.md` | Add FEAT-018 row |

## Verification

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
./gradlew lint
```

## Rollback

Single-line version revert if anything surfaces post-merge that unit tests didn't catch (e.g. a runtime-only behavior change). Low risk given the API-usage audit in the spec.
