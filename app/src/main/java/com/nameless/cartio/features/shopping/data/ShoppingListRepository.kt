package com.nameless.cartio.features.shopping.data

import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getShoppingLists(): Flow<List<ShoppingList>>
}
