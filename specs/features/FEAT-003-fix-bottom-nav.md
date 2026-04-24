# FEAT-003 — Fix & Restyle Bottom Navigation Bar

## Problem

The bottom navigation bar introduced in FEAT-002 is clipped/hidden behind the device's system navigation bar (gesture strip or 3-button strip) because `enableEdgeToEdge()` extends the Scaffold behind system UI but the custom `Box` wrapping the nav bar does not apply system navigation bar window insets.

Additionally the visual style does not match the prototype: the active tab should use a full-width orange pill with white icon + uppercase label, and the Reports icon was a placeholder (`ShoppingCart`).

## Decisions

- **Layout fix**: Add `Modifier.navigationBarsPadding()` to the outer Box so it floats above the system nav strip at all times.
- **Visual redesign**: Replace `NavigationBar`/`NavigationBarItem` (small M3 indicator) with a custom `Row`-based layout giving a full-width active pill per tab.
- **Third tab**: Keep "Settings" (not "Profile").
- **Icons**: Shopping → `ShoppingCart`, Reports → `BarChart`, Settings → `Settings`.
- **Labels**: Uppercase, `labelSmall` weight SemiBold.

## Scope

Single file change: `MainActivity.kt` bottomBar composable.
