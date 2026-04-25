package com.nameless.cartio.features.shopping.data

data class ShoppingListItem(
    val id: Long,
    val listId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val checked: Boolean,
    val note: String?
)
