# Plan: FEAT-022 — Upload Native Debug Symbols

## Implementation Order

1. Add `ndk { debugSymbolLevel = "FULL" }` to the `release` block in `app/build.gradle.kts`.
2. Build `bundleRelease` and confirm a native-debug-symbols artifact is produced.
3. `specs/README.md`: add FEAT-022 row.

No TDD step — build-config only, nothing to unit test.

## Files Modified

| File | Change |
|---|---|
| `app/build.gradle.kts` | Add `ndk.debugSymbolLevel = "FULL"` to `release` build type |
| `specs/features/FEAT-022-native-debug-symbols.md` | This spec |
| `specs/plans/FEAT-022-native-debug-symbols.md` | This plan |
| `specs/README.md` | Add FEAT-022 row |

## Verification

```bash
./gradlew bundleRelease
```

Confirm the build output includes a native-debug-symbols zip, typically at `app/build/outputs/bundle/release/app-release.aab` alongside generated native symbol artifacts under `app/build/intermediates/merged_native_libs/release/` or AGP's dedicated native-symbol-table task output — check the build log for a `stripReleaseDebugSymbols`/bundle-symbols task running, confirming the setting took effect.
