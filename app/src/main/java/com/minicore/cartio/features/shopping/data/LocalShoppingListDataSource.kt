package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.dao.ShoppingListDao
import com.minicore.cartio.core.database.dao.ShoppingListWithCount
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalShoppingListDataSource @Inject constructor(
    private val dao: ShoppingListDao
) : ShoppingListLocalDataSource {
    override fun getShoppingListsWithCount(): Flow<List<ShoppingListWithCount>> = dao.getAllWithItemCount()
    override fun getShoppingListByIdFlow(id: Long): Flow<ShoppingListEntity?> = dao.getByIdFlow(id)
    override suspend fun insert(entity: ShoppingListEntity): Long = dao.insert(entity)
    override suspend fun updateName(id: Long, name: String, updatedAt: Long) = dao.updateName(id, name, updatedAt)
    override suspend fun updateTimestamp(id: Long, updatedAt: Long) = dao.updateTimestamp(id, updatedAt)
    override suspend fun deleteById(id: Long) = dao.deleteById(id)
}
