package com.nameless.cartio.features.reports

import com.nameless.cartio.features.expenses.data.FakeExpenseRepository
import com.nameless.cartio.features.reports.domain.GetSpendingReportUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalCoroutinesApi::class)
class ReportsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(testDispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    private val repository = FakeExpenseRepository()

    private fun createViewModel() = ReportsViewModel(
        expenseRepository = repository,
        getSpendingReport = GetSpendingReportUseCase(repository)
    )

    @Test
    fun `init triggers purge of records older than 30 days`() = runTest {
        val beforeCreate = System.currentTimeMillis()
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(1, repository.purgedBefore.size)
        val cutoff = repository.purgedBefore[0]
        val expectedCutoff = beforeCreate - TimeUnit.DAYS.toMillis(30)
        assertTrue(
            "Purge cutoff $cutoff should be within 1 second of expected $expectedCutoff",
            cutoff >= expectedCutoff - 1_000L && cutoff <= expectedCutoff + 1_000L
        )
    }
}
