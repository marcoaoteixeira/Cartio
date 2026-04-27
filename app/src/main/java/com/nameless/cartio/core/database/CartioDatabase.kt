package com.nameless.cartio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.nameless.cartio.core.database.converters.CustomConverters
import com.nameless.cartio.core.database.dao.PriceHistoryDao
import com.nameless.cartio.core.database.dao.ProductDao
import com.nameless.cartio.core.database.dao.ShoppingListDao
import com.nameless.cartio.core.database.dao.ShoppingListItemDao
import com.nameless.cartio.core.database.entity.PriceHistoryEntity
import com.nameless.cartio.core.database.entity.ProductEntity
import com.nameless.cartio.core.database.entity.ShoppingListEntity
import com.nameless.cartio.core.database.entity.ShoppingListItemEntity
import com.nameless.cartio.features.expenses.data.ExpenseRecordDao
import com.nameless.cartio.features.expenses.data.ExpenseRecordEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ProductEntity::class,
        ShoppingListItemEntity::class,
        PriceHistoryEntity::class,
        ExpenseRecordEntity::class,
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(CustomConverters::class)
abstract class CartioDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun productDao(): ProductDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun priceHistoryDao(): PriceHistoryDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
}
