package com.minicore.cartio.features.expenses.domain

import com.minicore.cartio.features.expenses.data.ExpenseRepository
import javax.inject.Inject

class RecordExpensesUseCaseImpl @Inject constructor(
    private val repository: ExpenseRepository
) : RecordExpensesUseCase {
    override suspend fun invoke(records: List<ExpenseRecord>) {
        repository.recordExpenses(records)
    }
}
