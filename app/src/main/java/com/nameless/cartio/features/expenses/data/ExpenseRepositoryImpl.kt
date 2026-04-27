package com.nameless.cartio.features.expenses.data

import com.nameless.cartio.features.expenses.domain.ExpenseRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val dao: ExpenseRecordDao
) : ExpenseRepository {

    override suspend fun recordExpenses(records: List<ExpenseRecord>) {
        dao.insertAll(records.map { it.toEntity() })
    }

    override fun getRecordsSince(since: Long): Flow<List<ExpenseRecord>> =
        dao.getRecordsSince(since).map { list -> list.map { it.toDomain() } }

    override suspend fun purgeOlderThan(before: Long) = dao.deleteOlderThan(before)

    private fun ExpenseRecord.toEntity() = ExpenseRecordEntity(
        productName = productName,
        quantity = quantity,
        unitPrice = unitPrice,
        measureUnit = measureUnit,
        recordedAt = recordedAt
    )

    private fun ExpenseRecordEntity.toDomain() = ExpenseRecord(
        productName = productName,
        quantity = quantity,
        unitPrice = unitPrice,
        measureUnit = measureUnit,
        recordedAt = recordedAt
    )
}
