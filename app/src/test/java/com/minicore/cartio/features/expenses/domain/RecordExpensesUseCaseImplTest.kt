package com.minicore.cartio.features.expenses.domain

import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.features.expenses.data.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecordExpensesUseCaseImplTest {

    @Test
    fun `invoke delegates records to repository`() = runTest {
        val repository = FakeExpenseRepository()
        val useCase = RecordExpensesUseCaseImpl(repository)
        val records = listOf(
            ExpenseRecord("Milk", quantity = 2, unitPrice = 1.50, measureUnit = MeasureUnit.Piece, recordedAt = 1000L),
            ExpenseRecord("Bread", quantity = 1, unitPrice = 3.00, measureUnit = MeasureUnit.Piece, recordedAt = 1000L)
        )

        useCase.invoke(records)

        assertEquals(records, repository.recorded)
    }

    @Test
    fun `invoke with empty list delegates empty list to repository`() = runTest {
        val repository = FakeExpenseRepository()
        val useCase = RecordExpensesUseCaseImpl(repository)

        useCase.invoke(emptyList())

        assertEquals(emptyList<ExpenseRecord>(), repository.recorded)
    }

    @Test
    fun `invoke can be called multiple times and records accumulate`() = runTest {
        val repository = FakeExpenseRepository()
        val useCase = RecordExpensesUseCaseImpl(repository)
        val first = listOf(ExpenseRecord("Eggs", 1, 2.0, MeasureUnit.Piece, 1000L))
        val second = listOf(ExpenseRecord("Juice", 1, 4.0, MeasureUnit.Piece, 2000L))

        useCase.invoke(first)
        useCase.invoke(second)

        assertEquals(first + second, repository.recorded)
    }
}

private class FakeExpenseRepository : ExpenseRepository {
    val recorded = mutableListOf<ExpenseRecord>()
    override suspend fun recordExpenses(records: List<ExpenseRecord>) { recorded.addAll(records) }
    override fun getRecordsSince(since: Long): Flow<List<ExpenseRecord>> = flowOf(emptyList())
    override suspend fun purgeOlderThan(before: Long) {}
}
