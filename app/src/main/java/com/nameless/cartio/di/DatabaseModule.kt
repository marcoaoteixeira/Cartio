package com.nameless.cartio.di

import android.content.Context
import androidx.room.Room
import com.nameless.cartio.BuildConfig
import com.nameless.cartio.core.database.CartioDatabase
import com.nameless.cartio.core.database.dao.ProductDao
import com.nameless.cartio.core.database.dao.ShoppingListDao
import com.nameless.cartio.core.database.dao.ShoppingListItemDao
import com.nameless.cartio.features.shopping.data.LocalProductDataSource
import com.nameless.cartio.features.shopping.data.LocalShoppingListDataSource
import com.nameless.cartio.features.shopping.data.LocalShoppingListItemDataSource
import com.nameless.cartio.features.shopping.data.ProductLocalDataSource
import com.nameless.cartio.features.shopping.data.ShoppingListItemLocalDataSource
import com.nameless.cartio.features.shopping.data.ShoppingListItemRepository
import com.nameless.cartio.features.shopping.data.ShoppingListItemRepositoryImpl
import com.nameless.cartio.features.shopping.data.ShoppingListLocalDataSource
import com.nameless.cartio.features.shopping.data.ShoppingListRepository
import com.nameless.cartio.features.shopping.data.ShoppingListRepositoryImpl
import com.nameless.cartio.core.database.migrations.MIGRATION_2_3
import com.nameless.cartio.features.expenses.data.ExpenseRecordDao
import com.nameless.cartio.features.expenses.data.ExpenseRepository
import com.nameless.cartio.features.expenses.data.ExpenseRepositoryImpl
import com.nameless.cartio.features.expenses.domain.RecordExpensesUseCase
import com.nameless.cartio.features.settings.domain.ClearAllData
import com.nameless.cartio.features.settings.domain.ClearAllDataUseCase
import com.nameless.cartio.features.shopping.domain.AddItemToList
import com.nameless.cartio.features.shopping.domain.AddItemToListUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {

    @Binds
    @Singleton
    abstract fun bindLocalDataSource(impl: LocalShoppingListDataSource): ShoppingListLocalDataSource

    @Binds
    @Singleton
    abstract fun bindRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindItemLocalDataSource(impl: LocalShoppingListItemDataSource): ShoppingListItemLocalDataSource

    @Binds
    @Singleton
    abstract fun bindItemRepository(impl: ShoppingListItemRepositoryImpl): ShoppingListItemRepository

    @Binds
    @Singleton
    abstract fun bindProductLocalDataSource(impl: LocalProductDataSource): ProductLocalDataSource

    @Binds
    @Singleton
    abstract fun bindAddItemToList(impl: AddItemToListUseCase): AddItemToList

    @Binds
    @Singleton
    abstract fun bindClearAllData(impl: ClearAllDataUseCase): ClearAllData

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    companion object {
        private const val DATABASE_NAME = "cartio.db"

        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): CartioDatabase =
            Room.databaseBuilder(context, CartioDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_2_3)
                .apply { if (BuildConfig.DEBUG) fallbackToDestructiveMigration(dropAllTables = true) }
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

        @Provides
        fun provideRecordExpenses(repo: ExpenseRepository): RecordExpensesUseCase =
            RecordExpensesUseCase { records -> repo.recordExpenses(records) }
    }
}
