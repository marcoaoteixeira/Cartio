package com.minicore.cartio.features.expenses.data

import com.minicore.cartio.features.expenses.domain.ExpenseRecord
import com.minicore.cartio.features.expenses.domain.RecordExpensesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeExpenseRepository : ExpenseRepository, RecordExpensesUseCase {

    private val records = mutableListOf<ExpenseRecord>()
    val purgedBefore = mutableListOf<Long>()

    private val allRecords = MutableStateFlow<List<ExpenseRecord>>(emptyList())

    override suspend fun invoke(records: List<ExpenseRecord>) = recordExpenses(records)

    override suspend fun recordExpenses(records: List<ExpenseRecord>) {
        this.records.addAll(records)
        allRecords.value = this.records.toList()
    }

    override fun getRecordsSince(since: Long): Flow<List<ExpenseRecord>> =
        allRecords.map { list -> list.filter { it.recordedAt >= since } }

    override suspend fun purgeOlderThan(before: Long) {
        purgedBefore.add(before)
        records.removeAll { it.recordedAt < before }
        allRecords.value = records.toList()
    }

    fun seedRecords(vararg seeded: ExpenseRecord) {
        records.addAll(seeded)
        allRecords.value = records.toList()
    }
}
