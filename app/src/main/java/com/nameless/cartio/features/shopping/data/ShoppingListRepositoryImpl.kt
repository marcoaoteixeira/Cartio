package com.nameless.cartio.features.shopping.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ShoppingListRepositoryImpl @Inject constructor(
    private val localDataSource: ShoppingListLocalDataSource
) : ShoppingListRepository {
    override fun getShoppingLists(): Flow<List<ShoppingList>> =
        localDataSource.getShoppingLists().map { entities -> entities.map { it.toDomain() } }
}
