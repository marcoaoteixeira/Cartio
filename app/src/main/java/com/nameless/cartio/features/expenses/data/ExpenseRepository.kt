package com.nameless.cartio.features.expenses.data

import com.nameless.cartio.features.expenses.domain.ExpenseRecord
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    suspend fun recordExpenses(records: List<ExpenseRecord>)
    fun getRecordsSince(since: Long): Flow<List<ExpenseRecord>>
    suspend fun purgeOlderThan(before: Long)
}
