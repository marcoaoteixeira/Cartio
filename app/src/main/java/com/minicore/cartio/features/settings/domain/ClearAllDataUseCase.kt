package com.minicore.cartio.features.settings.domain

import com.minicore.cartio.core.database.dao.ProductDao
import com.minicore.cartio.core.database.dao.ShoppingListDao
import javax.inject.Inject

class ClearAllDataUseCase @Inject constructor(
    private val listDao: ShoppingListDao,
    private val productDao: ProductDao
) : ClearAllData {
    override suspend fun invoke() {
        listDao.clearAll()    // shopping_list_items cascade-delete via FK
        productDao.clearAll()
    }
}
