package com.minicore.cartio.features.shopping.domain

import com.minicore.cartio.core.database.entity.ProductEntity
import com.minicore.cartio.features.shopping.data.ProductLocalDataSource
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import javax.inject.Inject

class AddItemToListUseCase @Inject constructor(
    private val productDataSource: ProductLocalDataSource,
    private val itemRepository: ShoppingListItemRepository,
    private val listRepository: ShoppingListRepository
) : AddItemToList {

    override suspend fun invoke(listId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val existing = productDataSource.getByName(trimmed)
        val productId = existing?.id
            ?: productDataSource.insert(ProductEntity(name = trimmed, createdAt = System.currentTimeMillis()))
        val activeItem = itemRepository.findActiveItemByProduct(listId, productId)
        if (activeItem != null) {
            itemRepository.updateQuantity(activeItem.first, activeItem.second + 1)
        } else {
            itemRepository.insertItem(listId, productId)
        }
        listRepository.touchUpdatedAt(listId)
    }
}
