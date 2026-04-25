package com.nameless.cartio.features.shopping.domain

fun interface AddItemToList {
    suspend operator fun invoke(listId: Long, name: String)
}
