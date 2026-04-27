package com.nameless.cartio.features.expenses.domain

fun interface RecordExpensesUseCase {
    suspend operator fun invoke(records: List<ExpenseRecord>)
}
