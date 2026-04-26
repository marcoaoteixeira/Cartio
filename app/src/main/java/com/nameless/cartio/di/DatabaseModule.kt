package com.nameless.cartio.di

import android.content.Context
import androidx.room.Room
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

    companion object {
        @Provides
        @Singleton
        fun provideDatabase(@ApplicationContext context: Context): CartioDatabase =
            Room.databaseBuilder(context, CartioDatabase::class.java, "cartio.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
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
    }
}
