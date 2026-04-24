package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.entity.ShoppingListEntity

fun ShoppingListEntity.toDomain() = ShoppingList(
    id = id,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)
