package com.nameless.cartio.features.shopping.data

data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int = 0,
    val checkedCount: Int = 0
)
