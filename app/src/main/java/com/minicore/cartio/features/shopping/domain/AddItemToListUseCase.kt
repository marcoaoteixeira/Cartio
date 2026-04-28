package com.minicore.cartio.features.shopping.domain

import com.minicore.cartio.core.database.entity.ProductEntity
import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.features.shopping.data.ProductLocalDataSource
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import javax.inject.Inject

class AddItemToListUseCase @Inject constructor(
    private val productDataSource: ProductLocalDataSource,
    private val itemRepository: ShoppingListItemRepository,
    private val listRepository: ShoppingListRepository,
    private val clock: Clock
) : AddItemToList {

    override suspend fun invoke(listId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val existing = productDataSource.getByName(trimmed)
        val productId = existing?.id
            ?: productDataSource.insert(ProductEntity(name = trimmed, createdAt = clock.now()))
        val activeItem = itemRepository.findActiveItemByProduct(listId, productId)
        if (activeItem != null) {
            itemRepository.updateQuantity(activeItem.first, activeItem.second + 1)
        } else {
            itemRepository.insertItem(listId, productId)
        }
        listRepository.touchUpdatedAt(listId)
    }
}
