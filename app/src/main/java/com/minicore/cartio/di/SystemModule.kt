package com.minicore.cartio.di

import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.core.time.SystemClock
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SystemModule {

    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClock): Clock
}
