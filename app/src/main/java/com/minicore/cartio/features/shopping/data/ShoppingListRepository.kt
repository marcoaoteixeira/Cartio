package com.minicore.cartio.features.shopping.data

import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    fun getShoppingLists(): Flow<List<ShoppingList>>
    fun getShoppingListById(id: Long): Flow<ShoppingList?>
    suspend fun createShoppingList(name: String): Long
    suspend fun renameShoppingList(id: Long, name: String)
    suspend fun deleteShoppingList(id: Long)
    suspend fun touchUpdatedAt(id: Long)
}
