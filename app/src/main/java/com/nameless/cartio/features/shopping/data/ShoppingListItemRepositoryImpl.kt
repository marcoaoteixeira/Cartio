package com.nameless.cartio.features.shopping.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListItemRepositoryImpl @Inject constructor(
    private val localDataSource: ShoppingListItemLocalDataSource
) : ShoppingListItemRepository {

    override fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>> =
        localDataSource.getItemsForList(listId).map { list -> list.map { it.toDomain() } }

    override suspend fun insertItem(listId: Long, productId: Long) {
        localDataSource.insert(listId, productId)
    }

    override suspend fun updateQuantity(itemId: Long, quantity: Int) =
        localDataSource.updateQuantity(itemId, quantity.toFloat())

    override suspend fun checkItem(itemId: Long, checked: Boolean) =
        localDataSource.updateChecked(itemId, checked)

    override suspend fun deleteItem(itemId: Long) = localDataSource.deleteById(itemId)
}
