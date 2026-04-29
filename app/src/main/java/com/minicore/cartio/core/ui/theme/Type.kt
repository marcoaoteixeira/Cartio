package com.minicore.cartio.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.minicore.cartio.R

/*
 * Cartio typography.
 *
 * The brand wordmark uses the custom Autolova script font, applied via
 * `AutolovaFamily` — used in the splash and the navigation drawer header.
 * Body and label scales otherwise follow Material 3 defaults so dynamic
 * text scaling remains predictable.
 */
val AutolovaFamily = FontFamily(Font(R.font.autolova))

val CartioTypography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.W600,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
