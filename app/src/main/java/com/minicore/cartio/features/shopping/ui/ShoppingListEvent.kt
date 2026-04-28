package com.minicore.cartio.features.shopping.ui

sealed interface ShoppingListEvent {
    data class NavigateToDetail(val listId: Long) : ShoppingListEvent
    data class ListDeleted(val listId: Long, val listName: String) : ShoppingListEvent
}

sealed interface ShoppingListDetailEvent {
    data object NavigateUp : ShoppingListDetailEvent
    data class ItemDeleted(val itemId: Long, val productName: String) : ShoppingListDetailEvent
    data object ExpensesRecorded : ShoppingListDetailEvent
}
