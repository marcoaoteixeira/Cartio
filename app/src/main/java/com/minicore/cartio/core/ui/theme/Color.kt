package com.minicore.cartio.core.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * Cartio brand palette.
 *
 * The orange/cream system is the product's identity, not a placeholder.
 * Primary `#E65100` is warm without being shouty against the cream
 * `#FFF8F0` background. Tertiary yellow accents promo surfaces and
 * sort indicators. Swipe-action greens/reds are tuned for green=safe /
 * red=destructive at-a-glance.
 *
 * If you change a value here, audit `CartioTheme` light + dark mappings
 * and the brand cards in `SettingsComponents.kt` (PromoCard,
 * PurchasedCard) which paint with these directly.
 */

// Light palette (from prototype design tokens)
val Primary = Color(0xFFE65100)
val PrimaryDark = Color(0xFFBF360C)
val PrimaryLight = Color(0xFFFF8A65)
val Secondary = Color(0xFF795548)
val Tertiary = Color(0xFFFFB300)
val Background = Color(0xFFFFF8F0)
val Surface = Color(0xFFFFFFFF)
val SurfaceVariant = Color(0xFFF5F0EB)
val Error = Color(0xFFD32F2F)
val OnPrimary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF1C1B1F)
val OnSurface = Color(0xFF1C1B1F)
val Outline = Color(0xFF79747E)
val OutlineVariant = Color(0xFFCAC4D0)

// Swipe action backgrounds
val SwipeCheckColor = Color(0xFF4CAF50)
val SwipeDeleteColor = Color(0xFFE53935)

// Promo card
val BrandYellow = Color(0xFFFFCA28)
val PromoGradientStart = Color(0xFFFF8F00)
val PromoButtonDark = Color(0xFF1A1A1A)

// Dark palette
val PrimaryDarkTheme = Color(0xFFFF8A65)
val OnPrimaryDarkTheme = Color(0xFF8B2500)
val BackgroundDark = Color(0xFF1C1412)
val SurfaceDark = Color(0xFF231A17)
val SurfaceVariantDark = Color(0xFF3A2E2A)
val OnSurfaceDark = Color(0xFFF5EBE7) // bumped from #EDE0DC to clear WCAG AA on surfaceVariantDark
val OutlineDark = Color(0xFF9C8D88)
val OutlineVariantDark = Color(0xFF534340)
