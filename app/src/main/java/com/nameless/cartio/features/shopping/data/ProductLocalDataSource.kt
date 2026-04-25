package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.entity.ProductEntity

interface ProductLocalDataSource {
    suspend fun getByName(name: String): ProductEntity?
    suspend fun insert(entity: ProductEntity): Long
}
