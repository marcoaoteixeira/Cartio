package com.minicore.cartio.features.shopping.ui

import com.minicore.cartio.features.shopping.domain.ShoppingListItem

sealed interface ShoppingListEvent {
    data class NavigateToDetail(val listId: Long) : ShoppingListEvent
}

sealed interface ShoppingListDetailEvent {
    data object NavigateUp : ShoppingListDetailEvent
    data class ItemDeleted(val item: ShoppingListItem) : ShoppingListDetailEvent
}
