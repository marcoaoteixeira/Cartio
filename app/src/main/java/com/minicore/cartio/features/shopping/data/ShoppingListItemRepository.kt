package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.features.shopping.domain.ShoppingListItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListItemRepository {
    fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>>
    suspend fun findActiveItemByProduct(listId: Long, productId: Long): Pair<Long, Int>?
    suspend fun insertItem(listId: Long, productId: Long)
    suspend fun addOrIncrement(listId: Long, productId: Long)
    suspend fun updateQuantity(itemId: Long, quantity: Int)
    suspend fun checkItem(itemId: Long, checked: Boolean)
    suspend fun deleteItem(itemId: Long)
}
