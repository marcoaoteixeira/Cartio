package com.minicore.cartio.di

import com.minicore.cartio.features.expenses.domain.RecordExpensesUseCase
import com.minicore.cartio.features.expenses.domain.RecordExpensesUseCaseImpl
import com.minicore.cartio.features.settings.domain.ClearAllData
import com.minicore.cartio.features.settings.domain.ClearAllDataUseCase
import com.minicore.cartio.features.shopping.domain.AddItemToList
import com.minicore.cartio.features.shopping.domain.AddItemToListUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    @Singleton
    abstract fun bindAddItemToList(impl: AddItemToListUseCase): AddItemToList

    @Binds
    @Singleton
    abstract fun bindClearAllData(impl: ClearAllDataUseCase): ClearAllData

    @Binds
    @Singleton
    abstract fun bindRecordExpenses(impl: RecordExpensesUseCaseImpl): RecordExpensesUseCase
}
