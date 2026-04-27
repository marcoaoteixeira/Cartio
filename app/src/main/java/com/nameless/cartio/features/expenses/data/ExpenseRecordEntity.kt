package com.nameless.cartio.features.expenses.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.nameless.cartio.core.database.entity.MeasureUnit

@Entity(tableName = "expense_records")
data class ExpenseRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val measureUnit: MeasureUnit,
    val recordedAt: Long
)
