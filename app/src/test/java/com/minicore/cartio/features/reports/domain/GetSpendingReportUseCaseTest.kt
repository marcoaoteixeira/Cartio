package com.minicore.cartio.features.reports.domain

import com.minicore.cartio.core.database.entity.MeasureUnit
import com.minicore.cartio.features.expenses.data.FakeExpenseRepository
import com.minicore.cartio.features.expenses.domain.ExpenseRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class GetSpendingReportUseCaseTest {

    private val repository = FakeExpenseRepository()
    private val useCase = GetSpendingReportUseCase(repository)

    private val now = System.currentTimeMillis()
    private val clock: () -> Long = { now }

    private fun record(name: String, price: Double, qty: Int = 1, daysAgo: Long = 0L) = ExpenseRecord(
        productName = name,
        quantity = qty,
        unitPrice = price,
        measureUnit = MeasureUnit.Piece,
        recordedAt = now - TimeUnit.DAYS.toMillis(daysAgo)
    )

    @Test
    fun `empty repository returns zero total and empty top items`() = runTest {
        val report = useCase(clock).first()
        assertEquals(0.0, report.totalSpent, 0.001)
        assertEquals(emptyList<ItemSpending>(), report.topItems)
    }

    @Test
    fun `records within 30 days are included`() = runTest {
        repository.seedRecords(record("Milk", 2.0, daysAgo = 0))
        val report = useCase(clock).first()
        assertEquals(2.0, report.totalSpent, 0.001)
    }

    @Test
    fun `records older than 30 days are excluded`() = runTest {
        repository.seedRecords(
            record("Milk", 2.0, daysAgo = 0),
            record("Eggs", 5.0, daysAgo = 31)
        )
        val report = useCase(clock).first()
        assertEquals(2.0, report.totalSpent, 0.001)
        assertEquals(1, report.topItems.size)
        assertEquals("Milk", report.topItems[0].productName)
    }

    @Test
    fun `total correctly multiplies unit price by quantity`() = runTest {
        repository.seedRecords(record("Milk", 1.50, qty = 3, daysAgo = 0))
        val report = useCase(clock).first()
        assertEquals(4.50, report.totalSpent, 0.001)
    }

    @Test
    fun `top items are sorted by total spent descending`() = runTest {
        repository.seedRecords(
            record("Bread", 1.0, qty = 2),
            record("Milk", 5.0, qty = 1),
            record("Eggs", 2.0, qty = 3)
        )
        val report = useCase(clock).first()
        assertEquals("Eggs", report.topItems[0].productName)  // 6.0
        assertEquals("Milk", report.topItems[1].productName)  // 5.0
        assertEquals("Bread", report.topItems[2].productName) // 2.0
    }

    @Test
    fun `same product aggregated across multiple records`() = runTest {
        repository.seedRecords(
            record("Milk", 1.0),
            record("Milk", 2.0),
            record("Milk", 3.0)
        )
        val report = useCase(clock).first()
        assertEquals(1, report.topItems.size)
        assertEquals("Milk", report.topItems[0].productName)
        assertEquals(3, report.topItems[0].purchaseCount)
        assertEquals(6.0, report.topItems[0].totalSpent, 0.001)
    }

    @Test
    fun `top items limited to 10`() = runTest {
        (1..15).forEach { i -> repository.seedRecords(record("Item$i", i.toDouble())) }
        val report = useCase(clock).first()
        assertEquals(10, report.topItems.size)
    }
}
