package com.nameless.cartio.features.shopping.data

import com.nameless.cartio.core.database.dao.ProductDao
import com.nameless.cartio.core.database.entity.ProductEntity
import javax.inject.Inject

class LocalProductDataSource @Inject constructor(
    private val dao: ProductDao
) : ProductLocalDataSource {
    override suspend fun getByName(name: String): ProductEntity? = dao.getByName(name)
    override suspend fun insert(entity: ProductEntity): Long = dao.insert(entity)
}
