package com.minicore.cartio.features.shopping.data

import com.minicore.cartio.core.database.entity.ProductEntity

interface ProductLocalDataSource {
    suspend fun getByName(name: String): ProductEntity?
    suspend fun insert(entity: ProductEntity): Long
}
