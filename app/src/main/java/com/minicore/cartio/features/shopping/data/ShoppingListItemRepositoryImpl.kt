package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.features.shopping.domain.ShoppingListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListItemRepositoryImpl @Inject constructor(
    private val localDataSource: ShoppingListItemLocalDataSource
) : ShoppingListItemRepository {

    override fun getItemsForList(listId: Long): Flow<List<ShoppingListItem>> =
        localDataSource.getItemsForList(listId).map { list -> list.map { it.toDomain() } }

    override suspend fun findActiveItemByProduct(listId: Long, productId: Long): Pair<Long, Int>? {
        val entity = localDataSource.findActiveByProduct(listId, productId) ?: return null
        return Pair(entity.id, entity.quantity)
    }

    override suspend fun insertItem(listId: Long, productId: Long) {
        localDataSource.insert(listId, productId)
    }

    override suspend fun addOrIncrement(listId: Long, productId: Long) =
        localDataSource.addOrIncrement(listId, productId)

    override suspend fun restoreItem(item: ShoppingListItem) {
        localDataSource.insertEntity(
            ShoppingListItemEntity(
                shoppingListId = item.listId,
                productId = item.productId,
                quantity = item.quantity,
                checked = item.checked,
                note = item.note
            )
        )
    }

    override suspend fun updateQuantity(itemId: Long, quantity: Int) =
        localDataSource.updateQuantity(itemId, quantity)

    override suspend fun checkItem(itemId: Long, checked: Boolean) =
        localDataSource.updateChecked(itemId, checked)

    override suspend fun deleteItem(itemId: Long) = localDataSource.deleteById(itemId)
}
