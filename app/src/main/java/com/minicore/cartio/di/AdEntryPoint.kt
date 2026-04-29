package com.minicore.cartio.di

import com.minicore.cartio.features.monetization.domain.ShowDetailAdUseCase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Lets Composables retrieve [ShowDetailAdUseCase] without coupling the ViewModel to Activity. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface AdEntryPoint {
    fun showDetailAdUseCase(): ShowDetailAdUseCase
}
