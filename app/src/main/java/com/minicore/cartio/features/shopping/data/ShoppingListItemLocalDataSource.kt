package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlinx.coroutines.flow.Flow

interface ShoppingListItemLocalDataSource {
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItemWithProduct>>
    suspend fun findActiveByProduct(listId: Long, productId: Long): ShoppingListItemEntity?
    suspend fun insert(listId: Long, productId: Long): Long
    suspend fun updateQuantity(id: Long, quantity: Int)
    suspend fun updateChecked(id: Long, checked: Boolean)
    suspend fun deleteById(id: Long)
}
