package com.minicore.cartio.core.format

import java.text.NumberFormat
import java.util.Locale

/**
 * Currency formatting for Cartio.
 *
 * v1 spec is `$`-only (no locale variation). We pin to [Locale.US] so prices
 * render the same regardless of device locale. Concentrating the choice here
 * means a future "use device locale" toggle is a one-line change.
 */
object CurrencyFormat {

    private val formatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }

    fun format(value: Double): String = formatter.format(value)
}
