# FEAT-015 — Debug Ad Unit ID Selection

## Objective

Ensure debug builds always use Google's official test interstitial ad unit so development traffic never hits the production unit. The switch must be automatic — no manual step before a Play Store build.

## User Story

As a developer running a debug build on a real device, I see a clearly labelled **Test Ad** when an interstitial fires. When the same code is built as a release APK the production unit is used, with no code change required.

## Requirements

- A single resolver function in `MonetizationConfig` returns the correct unit ID based on the `debug` flag. Default is `BuildConfig.DEBUG` so production code needs no argument.
- The test unit ID constant is renamed `ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS` to make its purpose unambiguous.
- `AdMobDataSource` calls the resolver — it never references a raw unit ID string.
- Unit tests verify both branches of the resolver without touching the AdMob SDK.

## Decisions

| Decision | Choice |
|---|---|
| Where to put selection logic | `MonetizationConfig.interstitialUnitId(debug)` — keeps it with the other constants and makes it unit-testable |
| Why not inline at call site | `BuildConfig.DEBUG` is a compile-time constant; an explicit `debug` param lets tests pass `true`/`false` independently |
| Test unit ID constant name | `ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS` — distinguishes "used in tests" from "only visible in debug UI" |
