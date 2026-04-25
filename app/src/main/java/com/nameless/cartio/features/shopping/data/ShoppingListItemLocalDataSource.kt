package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlinx.coroutines.flow.Flow

interface ShoppingListItemLocalDataSource {
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItemWithProduct>>
    suspend fun insert(listId: Long, productId: Long): Long
    suspend fun updateQuantity(id: Long, quantity: Float)
    suspend fun updateChecked(id: Long, checked: Boolean)
    suspend fun deleteById(id: Long)
}
