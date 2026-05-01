package com.minicore.cartio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.minicore.cartio.core.database.converters.CustomConverters
import com.minicore.cartio.core.database.dao.ProductDao
import com.minicore.cartio.core.database.dao.ShoppingListDao
import com.minicore.cartio.core.database.dao.ShoppingListItemDao
import com.minicore.cartio.core.database.entity.ProductEntity
import com.minicore.cartio.core.database.entity.ShoppingListEntity
import com.minicore.cartio.core.database.entity.ShoppingListItemEntity
import com.minicore.cartio.features.expenses.data.ExpenseRecordDao
import com.minicore.cartio.features.expenses.data.ExpenseRecordEntity

@Database(
    entities = [
        ShoppingListEntity::class,
        ProductEntity::class,
        ShoppingListItemEntity::class,
        ExpenseRecordEntity::class,
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(CustomConverters::class)
abstract class CartioDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun productDao(): ProductDao
    abstract fun shoppingListItemDao(): ShoppingListItemDao
    abstract fun expenseRecordDao(): ExpenseRecordDao
}
