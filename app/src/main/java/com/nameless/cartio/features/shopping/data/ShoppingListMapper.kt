package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.dao.ShoppingListWithCount
import com.nameless.cartio.core.database.entity.ShoppingListEntity

fun ShoppingListEntity.toDomain() = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemCount = 0,
    checkedCount = 0
)

fun ShoppingListWithCount.toDomain() = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt,
    itemCount = itemCount,
    checkedCount = checkedCount
)
