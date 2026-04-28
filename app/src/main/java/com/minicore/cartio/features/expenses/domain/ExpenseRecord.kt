package com.minicore.cartio.features.expenses.domain

import com.minicore.cartio.core.database.entity.MeasureUnit

data class ExpenseRecord(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val measureUnit: MeasureUnit,
    val recordedAt: Long
)
