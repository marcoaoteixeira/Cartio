package com.nameless.cartio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nameless.cartio.core.database.dao.PriceHistoryDao
import com.nameless.cartio.core.database.dao.ProductDao
import com.nameless.cartio.core.database.dao.ShoppingListDao
import com.nameless.cartio.core.database.dao.ShoppingListItemDao
import com.nameless.cartio.core.database.entity.PriceHistoryEntity
import com.nameless.cartio.core.database.entity.ProductEntity
import com.nameless.cartio.core.database.entity.ShoppingListEntity
import com.nameless.cartio.core.database.entity.ShoppingListItemEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ProductEntity::class,
        ShoppingListItemEntity::class,
        PriceHistoryEntity::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class CartioDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun productDao(): ProductDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun priceHistoryDao(): PriceHistoryDao
}
