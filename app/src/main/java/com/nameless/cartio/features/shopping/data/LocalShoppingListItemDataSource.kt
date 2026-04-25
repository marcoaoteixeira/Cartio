package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.dao.ShoppingListItemDao
import com.nameless.cartio.core.database.entity.ShoppingListItemEntity
import com.nameless.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalShoppingListItemDataSource @Inject constructor(
    private val dao: ShoppingListItemDao
) : ShoppingListItemLocalDataSource {

    override fun getItemsForList(listId: Long): Flow<List<ShoppingListItemWithProduct>> =
        dao.getByListWithProduct(listId)

    override suspend fun insert(listId: Long, productId: Long): Long =
        dao.insert(ShoppingListItemEntity(shoppingListId = listId, productId = productId, quantity = 1f))

    override suspend fun updateQuantity(id: Long, quantity: Float) = dao.updateQuantity(id, quantity)

    override suspend fun updateChecked(id: Long, checked: Boolean) = dao.updateChecked(id, checked)

    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
