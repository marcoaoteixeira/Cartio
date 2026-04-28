package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.dao.ShoppingListWithCount
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

interface ShoppingListLocalDataSource {
    fun getShoppingListsWithCount(): Flow<List<ShoppingListWithCount>>
    fun getShoppingListsWithCountPaged(limit: Int, offset: Int): Flow<List<ShoppingListWithCount>>
    fun getShoppingListByIdFlow(id: Long): Flow<ShoppingListEntity?>
    suspend fun insert(entity: ShoppingListEntity): Long
    suspend fun updateName(id: Long, name: String, updatedAt: Long)
    suspend fun updateTimestamp(id: Long, updatedAt: Long)
    suspend fun deleteById(id: Long)
}
