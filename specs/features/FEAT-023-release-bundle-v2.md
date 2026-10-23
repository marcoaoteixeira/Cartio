# FEAT-023 — Release Bundle (versionCode 2)

## Objective

Produce a signed app bundle (`.aab`) for Play Console upload, now that FEAT-022's native-debug-symbols fix is on master. `versionCode 1` was already consumed by a prior Play Console upload, so this bundle must bump to `versionCode 2` or Play Console will reject it.

## Decisions

| Decision | Choice |
|---|---|
| versionCode | 1 → 2 |
| versionName | Unchanged (`1.0`) — still pre-launch, no user-facing version bump needed yet |
| Build command | `./gradlew bundleRelease` — produces `app/build/outputs/bundle/release/app-release.aab`, signed via `docs/keystore/keystore.properties` |

## Verification

- Confirm the AAB is signed (not using a debug signature) — `./gradlew bundleRelease` only applies the release `signingConfig` when `keystoreProps` is non-empty (verified present locally).
- Confirm `extractReleaseNativeDebugMetadata` task runs (FEAT-022's fix still in effect).
