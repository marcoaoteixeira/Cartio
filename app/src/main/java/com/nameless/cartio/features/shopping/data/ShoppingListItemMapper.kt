package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.entity.ShoppingListItemWithProduct
import kotlin.math.roundToInt

fun ShoppingListItemWithProduct.toDomain() = ShoppingListItem(
    id = item.id,
    listId = item.shoppingListId,
    productId = item.productId,
    productName = product.name,
    quantity = item.quantity?.roundToInt()?.coerceAtLeast(1) ?: 1,
    checked = item.checked,
    note = item.note
)
