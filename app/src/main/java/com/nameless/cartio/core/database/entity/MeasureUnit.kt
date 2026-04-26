package com.nameless.cartio.core.database.entity

enum class MeasureUnit(val abbreviation: String) {
    Piece("pcs"),
    Gram("g"),
    Kilogram("kg"),
    Milliliter("mL"),
    Liter("L");

    companion object {
        fun fromAbbreviation(value: String): MeasureUnit =
            entries.first { it.abbreviation == value }
    }
}