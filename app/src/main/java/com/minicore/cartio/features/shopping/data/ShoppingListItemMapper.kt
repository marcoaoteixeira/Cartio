package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ShoppingListItemWithProduct

fun ShoppingListItemWithProduct.toDomain() = ShoppingListItem(
    id = item.id,
    listId = item.shoppingListId,
    productId = item.productId,
    productName = product.name,
    quantity = item.quantity,
    checked = item.checked,
    note = item.note
)
