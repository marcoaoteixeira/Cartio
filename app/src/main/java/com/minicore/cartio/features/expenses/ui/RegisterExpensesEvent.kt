package com.minicore.cartio.features.expenses.ui

sealed interface RegisterExpensesEvent {
    data object SavedAndUp : RegisterExpensesEvent
}
