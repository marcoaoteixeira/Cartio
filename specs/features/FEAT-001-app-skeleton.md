# FEAT-001 — App Skeleton

**Status:** Done  
**Branch:** `feature/CARTIO-001-app-skeleton`

---

## Goal

Produce the bare-minimum Android project that can be installed on an emulator and opened by the user. No functional features — just a confirmed working foundation.

## Requirements

- App icon visible in the device launcher.
- Tapping the icon opens the app.
- The screen shows the text **"Cartio under construction"** centered on a blank background.
- No action bar.
- Builds successfully with `./gradlew assembleDebug`.

## Decisions

- **Jetpack Compose** is used for the UI from day one (ADR-001).
- **No Room, Hilt, or Navigation Compose** in this feature — added in later features to keep the skeleton minimal.
- **`MaterialTheme`** from Compose Material3 is used directly (no custom `CartioTheme` wrapper yet; that belongs in a future `core/ui/` module).
- **`enableEdgeToEdge()`** applied in `MainActivity` so Compose owns the full window from the start.
- **AGP 9.2.0 bundles Kotlin compilation** — `org.jetbrains.kotlin.android` is NOT applied separately (causes an extension conflict). Only `org.jetbrains.kotlin.plugin.compose` is applied explicitly.
- Theme parent changed from `DarkActionBar` → `NoActionBar` in both `values/themes.xml` and `values-night/themes.xml`.

## Out of Scope

- Any business logic, navigation, or data layer.
- Custom theming or branding.
