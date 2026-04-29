package com.minicore.cartio.di

import android.content.Context
import android.content.SharedPreferences
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.minicore.cartio.core.time.Clock
import com.minicore.cartio.features.monetization.data.AdFrequencyStore
import com.minicore.cartio.features.monetization.data.AdMobDataSource
import com.minicore.cartio.features.monetization.data.AdsRepositoryImpl
import com.minicore.cartio.features.monetization.data.BillingClientFacade
import com.minicore.cartio.features.monetization.data.BillingClientWrapper
import com.minicore.cartio.features.monetization.data.BillingRepositoryImpl
import com.minicore.cartio.features.monetization.data.PlayBillingClientFacade
import com.minicore.cartio.features.monetization.domain.AdsRepository
import com.minicore.cartio.features.monetization.domain.BillingRepository
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonetizationModule {

    @Provides
    @Singleton
    @Named("ad_frequency_prefs")
    fun provideAdFrequencyPrefs(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences(AdFrequencyStore.PREFS_FILE, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideAdFrequencyStore(
        @Named("ad_frequency_prefs") prefs: SharedPreferences,
        clock: Clock
    ): AdFrequencyStore = AdFrequencyStore(prefs, clock)

    @Provides
    @Singleton
    fun provideAdMobDataSource(): AdMobDataSource = AdMobDataSource()

    @Provides
    @Singleton
    fun providePurchasesUpdatedListener(
        wrapper: Lazy<BillingClientWrapper>
    ): PurchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { wrapper.get().handlePurchase(it) }
        }
    }

    @Provides
    @Singleton
    fun provideBillingClient(
        @ApplicationContext context: Context,
        listener: PurchasesUpdatedListener
    ): BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .setListener(listener)
        .build()

    @Provides
    @Singleton
    fun provideBillingClientFacade(client: BillingClient): BillingClientFacade =
        PlayBillingClientFacade(client)

    @Provides
    fun provideAdsRepository(
        adMob: AdMobDataSource,
        store: AdFrequencyStore
    ): AdsRepository = AdsRepositoryImpl(adMob, store)

    @Provides
    fun provideBillingRepository(wrapper: BillingClientWrapper): BillingRepository =
        BillingRepositoryImpl(wrapper)
}
