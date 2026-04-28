package com.minicore.cartio.core.database.entity

enum class MeasureUnit(val abbreviation: String) {
    Piece("pcs"),
    Gram("g"),
    Kilogram("kg"),
    Milliliter("mL"),
    Liter("L");

    companion object {
        // Falls back to Piece when an unknown abbreviation is read from the DB
        // (e.g. a downgrade from a future schema that introduced a new unit).
        fun fromAbbreviation(value: String): MeasureUnit =
            entries.firstOrNull { it.abbreviation == value } ?: Piece
    }
}