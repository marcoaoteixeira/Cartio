package com.minicore.cartio.core.database.converters

import androidx.room.TypeConverter
import com.minicore.cartio.core.database.entity.MeasureUnit

class CustomConverters {
    @TypeConverter
    fun fromMeasureUnit(measureUnit: MeasureUnit): String =
        measureUnit.abbreviation

    @TypeConverter
    fun toMeasureUnit(abbreviation: String): MeasureUnit =
        MeasureUnit.fromAbbreviation(abbreviation)
}
