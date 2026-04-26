package com.nameless.cartio.di

import com.nameless.cartio.features.backup.data.BackupPreferences
import com.nameless.cartio.features.backup.data.BackupPreferencesImpl
import com.nameless.cartio.features.backup.data.CartioBackupManagerImpl
import com.nameless.cartio.features.backup.domain.CartioBackupManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupModule {

    @Binds
    @Singleton
    abstract fun bindBackupManager(impl: CartioBackupManagerImpl): CartioBackupManager

    @Binds
    @Singleton
    abstract fun bindBackupPreferences(impl: BackupPreferencesImpl): BackupPreferences
}
