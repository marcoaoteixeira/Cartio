package com.minicore.cartio.di

import com.minicore.cartio.features.settings.data.AppLanguagePreferences
import com.minicore.cartio.features.settings.data.AppLanguagePreferencesImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LanguageModule {

    @Binds
    @Singleton
    abstract fun bindAppLanguagePreferences(impl: AppLanguagePreferencesImpl): AppLanguagePreferences
}
