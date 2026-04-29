package com.minicore.cartio.core.ui.tokens

import androidx.compose.ui.unit.dp

/**
 * Cartio's custom design tokens.
 *
 * The brand uses M3 colour and typography but layers its own component
 * geometry on top — bigger corner radii than M3 default, slightly more
 * generous spacing. Centralising the values here lets future screens stop
 * re-inventing `dp` numerals.
 */
object Spacing {
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Default = 16.dp
    val Large = 20.dp
    val ExtraLarge = 24.dp
}

object CornerRadius {
    val Small = 8.dp
    val Default = 12.dp
    val Large = 16.dp
    val Pill = 24.dp
    val Round = 36.dp
}

object Elevation {
    val None = 0.dp
    val Hairline = 1.dp
    val Card = 2.dp
}
