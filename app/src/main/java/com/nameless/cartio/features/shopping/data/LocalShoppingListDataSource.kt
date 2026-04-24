package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.dao.ShoppingListDao
import com.nameless.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalShoppingListDataSource @Inject constructor(
    private val dao: ShoppingListDao
) : ShoppingListLocalDataSource {
    override fun getShoppingLists(): Flow<List<ShoppingListEntity>> = dao.getAll()
}
