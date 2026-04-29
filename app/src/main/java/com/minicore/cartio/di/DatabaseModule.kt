package com.minicore.cartio.di

import android.content.Context
import androidx.room.Room
import com.minicore.cartio.core.config.AppConfig
import com.minicore.cartio.core.database.CartioDatabase
import com.minicore.cartio.core.database.dao.ProductDao
import com.minicore.cartio.core.database.dao.ShoppingListDao
import com.minicore.cartio.core.database.dao.ShoppingListItemDao
import com.minicore.cartio.core.database.migrations.ALL_MIGRATIONS
import com.minicore.cartio.features.expenses.data.ExpenseRecordDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "cartio.db"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CartioDatabase =
        Room.databaseBuilder(context, CartioDatabase::class.java, DATABASE_NAME)
            .addMigrations(*ALL_MIGRATIONS)
            .apply {
                if (AppConfig.DROP_DB_ON_MIGRATION_FAILURE) {
                    fallbackToDestructiveMigration(dropAllTables = true)
                }
            }
            .build()

    @Provides
    @Singleton
    fun provideShoppingListDao(db: CartioDatabase): ShoppingListDao = db.shoppingListDao()

    @Provides
    @Singleton
    fun provideShoppingListItemDao(db: CartioDatabase): ShoppingListItemDao = db.shoppingListItemDao()

    @Provides
    @Singleton
    fun provideProductDao(db: CartioDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideExpenseRecordDao(db: CartioDatabase): ExpenseRecordDao = db.expenseRecordDao()
}
