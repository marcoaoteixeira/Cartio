package com.minicore.cartio.features.shopping.domain

data class ShoppingList(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int = 0,
    val checkedCount: Int = 0
)
