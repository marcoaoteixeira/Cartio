package com.minicore.cartio.core.database.dao

data class ShoppingListWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val itemCount: Int,
    val checkedCount: Int
)
