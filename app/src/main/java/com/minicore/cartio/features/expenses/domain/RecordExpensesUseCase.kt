package com.minicore.cartio.features.expenses.domain

fun interface RecordExpensesUseCase {
    suspend operator fun invoke(records: List<ExpenseRecord>)
}
