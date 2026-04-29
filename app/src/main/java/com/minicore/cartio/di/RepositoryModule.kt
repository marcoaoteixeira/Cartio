package com.minicore.cartio.di

import com.minicore.cartio.features.expenses.data.ExpenseRepository
import com.minicore.cartio.features.expenses.data.ExpenseRepositoryImpl
import com.minicore.cartio.features.shopping.data.LocalProductDataSource
import com.minicore.cartio.features.shopping.data.LocalShoppingListDataSource
import com.minicore.cartio.features.shopping.data.LocalShoppingListItemDataSource
import com.minicore.cartio.features.shopping.data.ProductLocalDataSource
import com.minicore.cartio.features.shopping.data.ShoppingListItemLocalDataSource
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepository
import com.minicore.cartio.features.shopping.data.ShoppingListItemRepositoryImpl
import com.minicore.cartio.features.shopping.data.ShoppingListLocalDataSource
import com.minicore.cartio.features.shopping.data.ShoppingListRepository
import com.minicore.cartio.features.shopping.data.ShoppingListRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindShoppingListLocalDataSource(impl: LocalShoppingListDataSource): ShoppingListLocalDataSource

    @Binds
    @Singleton
    abstract fun bindShoppingListRepository(impl: ShoppingListRepositoryImpl): ShoppingListRepository

    @Binds
    @Singleton
    abstract fun bindShoppingListItemLocalDataSource(impl: LocalShoppingListItemDataSource): ShoppingListItemLocalDataSource

    @Binds
    @Singleton
    abstract fun bindShoppingListItemRepository(impl: ShoppingListItemRepositoryImpl): ShoppingListItemRepository

    @Binds
    @Singleton
    abstract fun bindProductLocalDataSource(impl: LocalProductDataSource): ProductLocalDataSource

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository
}
