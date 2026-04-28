# FEAT-001 — Implementation Plan: App Skeleton

## Ordered Steps

1. **`gradle/libs.versions.toml`** — Add `composeBom` version; add Compose library aliases (`compose-bom`, `compose-ui`, `compose-ui-tooling-preview`, `compose-ui-tooling`, `compose-material3`, `activity-compose`); add `compose-compiler` plugin alias.

2. **`build.gradle.kts` (root)** — Declare `compose-compiler` plugin with `apply false`.

3. **`app/build.gradle.kts`** — Apply `compose-compiler` plugin; add `buildFeatures { compose = true }` inside `android {}`; wire in Compose BOM + runtime dependencies.

4. **`app/src/main/AndroidManifest.xml`** — Add `<activity android:name=".MainActivity" android:exported="true">` with `MAIN`/`LAUNCHER` intent filter.

5. **`app/src/main/res/values/themes.xml`** and **`values-night/themes.xml`** — Change parent to `Theme.MaterialComponents.DayNight.NoActionBar`.

6. **`app/src/main/java/com/minicore/cartio/MainActivity.kt`** (create) — `ComponentActivity` with `enableEdgeToEdge()` + `setContent` rendering `UnderConstructionScreen`.

## Edge Cases Covered

- `android:exported="true"` is mandatory on API 31+ for any activity with a `LAUNCHER` intent filter (enforced as build error by AGP on compileSdk 36).
- `NoActionBar` theme parent prevents the system from inflating an action bar that would conflict with Compose owning the window.
- `debugImplementation` for `compose-ui-tooling` keeps Preview support out of release builds.
- `libs.material` MDC dependency is kept even though the UI uses Compose Material3, because the XML theme parent (`Theme.MaterialComponents`) is defined in that artifact.

## Verification

```bash
./gradlew assembleDebug   # must succeed
./gradlew test            # existing unit tests must still pass
./gradlew installDebug    # install on API 29+ emulator
```

Manual: launcher icon visible → tap → blank screen with "Cartio under construction" centered → no action bar.
