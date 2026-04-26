# FEAT-006 — Settings Screen Foundation

## Summary

Replaces the "Coming Soon" stub with a fully designed Settings screen matching the prototype. Delivers the complete UI and one functional action (Clear All Data).

## Sections

| Section | Description |
|---|---|
| Backup & Sync | Toggle row for Google Drive sync — UI state only, no real auth |
| Support Cartio | Promo card with gradient, buy button — no-op (no real IAP) |
| Data | Export JSON row (no-op) + Clear All Data row with confirmation dialog |
| About | App version from BuildConfig + Open-source licenses row (no-op) |
| Footer | "made with care · cartio vX.X" |

## Reusable Components

- `SettingsSection` — uppercase label + bordered rounded card container
- `SettingsListItem` — icon + title + subtitle + optional chevron + optional divider
- `ToggleSettingItem` — icon + title + subtitle + Switch
- `SettingsIconBox` — 40×40dp rounded colored icon background
- `PromoCard` — gradient card with badge, title, subtitle, action button

## Functional

- **Clear All Data**: confirmation dialog → `ClearAllDataUseCase` → `ShoppingListDao.clearAll()` (cascade-deletes items) + `ProductDao.clearAll()`
- **Version info**: sourced from `BuildConfig.VERSION_NAME` / `VERSION_CODE` (requires `buildConfig = true` in build features)
- **Sync toggle**: local `StateFlow<Boolean>` in `SettingsViewModel`, no external side effects

## Decisions

- `ClearAllDataUseCase` is a `fun interface` for easy test faking
- `SettingsViewModel.confirmClearData` uses `runCatching` so the dialog always closes even if Room throws
- `buildConfig = true` added to `app/build.gradle.kts` — needed for `BuildConfig` access in Compose
