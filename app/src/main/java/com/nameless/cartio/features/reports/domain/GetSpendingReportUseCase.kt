package com.nameless.cartio.features.reports.domain

import com.nameless.cartio.features.expenses.data.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class GetSpendingReportUseCase @Inject constructor(
    private val repository: ExpenseRepository
) {
    operator fun invoke(clock: () -> Long = System::currentTimeMillis): Flow<SpendingReport> {
        val since = clock() - TimeUnit.DAYS.toMillis(30)
        return repository.getRecordsSince(since).map { records ->
            val total = records.sumOf { it.unitPrice * it.quantity }
            val top10 = records
                .groupBy { it.productName }
                .map { (name, entries) ->
                    ItemSpending(
                        productName = name,
                        totalSpent = entries.sumOf { it.unitPrice * it.quantity },
                        purchaseCount = entries.size
                    )
                }
                .sortedByDescending { it.totalSpent }
                .take(10)
            SpendingReport(totalSpent = total, topItems = top10)
        }
    }
}
