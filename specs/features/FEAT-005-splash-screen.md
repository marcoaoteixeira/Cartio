# FEAT-005 — Splash Screen

## Summary

Branded splash screen shown on cold launch, covering the window before and during the first Compose frame draw. Uses the official `core-splashscreen` compat library for the OS-level portion and a custom Compose screen for the full branded experience.

## Requirements

- Display immediately on app launch (before Compose draws)
- Show the branded shopping cart icon on a white background
- Show "Cartio" text rendered with the Autolova custom font
- Show a yellow wave at the bottom of the screen
- Remain visible for approximately 2 seconds
- Update the launcher app icon to use the branded shopping cart image
- Rotation during splash must not restart the timer

## Design

Concept reference: `branding/splash_screen.png`

### Two-phase approach

| Phase | Library | Visible when |
|---|---|---|
| OS-level | `core-splashscreen` | Before first Compose frame |
| Compose-level | Custom composable | After first frame, for 2 seconds |

### Assets

| Asset | Source | Destination |
|---|---|---|
| Shopping cart icon | `branding/option-a/shopping-cart-512x512.png` | `res/drawable/shopping_cart.png` |
| Autolova font | `branding/Autolova.ttf` | `res/font/autolova.ttf` |

## Decisions

- Fixed 2-second delay: the app has no real startup initialization (Room is lazy); 2s gives the branding adequate visibility
- `rememberSaveable` for splash state: survives configuration changes so rotation doesn't re-trigger the splash
- White icon background: the cart PNG includes its own yellow circle, so no secondary yellow backing is added
- Monochrome adaptive icon layer removed: a bitmap drawable is not suitable for system-generated monochrome icons
