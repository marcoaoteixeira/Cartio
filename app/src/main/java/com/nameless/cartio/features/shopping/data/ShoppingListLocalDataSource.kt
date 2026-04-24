package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.entity.ShoppingListEntity
import kotlinx.coroutines.flow.Flow

interface ShoppingListLocalDataSource {
    fun getShoppingLists(): Flow<List<ShoppingListEntity>>
}
