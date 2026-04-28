package com.minicore.cartio.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ShoppingListItemWithProduct(
    @Embedded val item: ShoppingListItemEntity,
    @Relation(parentColumn = "productId", entityColumn = "id")
    val product: ProductEntity
)
