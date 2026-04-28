package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ShoppingListEntity
import com.minicore.cartio.core.time.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val localDataSource: ShoppingListLocalDataSource,
    private val clock: Clock
) : ShoppingListRepository {

    override fun getShoppingLists(): Flow<List<ShoppingList>> =
        localDataSource.getShoppingListsWithCount().map { list -> list.map { it.toDomain() } }

    override fun getShoppingListById(id: Long): Flow<ShoppingList?> =
        localDataSource.getShoppingListByIdFlow(id).map { it?.toDomain() }

    override suspend fun createShoppingList(name: String): Long {
        val now = clock.now()
        return localDataSource.insert(ShoppingListEntity(name = name, createdAt = now, updatedAt = now))
    }

    override suspend fun renameShoppingList(id: Long, name: String) =
        localDataSource.updateName(id, name, clock.now())

    override suspend fun deleteShoppingList(id: Long) = localDataSource.deleteById(id)

    override suspend fun touchUpdatedAt(id: Long) =
        localDataSource.updateTimestamp(id, clock.now())
}
