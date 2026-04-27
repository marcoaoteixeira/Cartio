package com.nameless.cartio.features.monetization.data

import android.app.Activity
import android.content.Context
import com.nameless.cartio.features.monetization.MonetizationConfig
import com.nameless.cartio.features.monetization.domain.AdResult
import com.nameless.cartio.features.monetization.domain.AdsRepository
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class AdsRepositoryImpl @Inject constructor(
    private val adMob: AdMobDataSource,
    private val frequencyStore: AdFrequencyStore
) : AdsRepository {

    override suspend fun preload(context: Context) = adMob.preload(context)

    override fun isAdAvailable(): Boolean = adMob.hasAd()

    override suspend fun shouldShowAd(): Boolean = frequencyStore.shouldShowAd()

    override suspend fun markAdShown() = frequencyStore.markShown()

    override suspend fun showInterstitial(activity: Activity): AdResult {
        return withTimeoutOrNull(MonetizationConfig.AD_TIMEOUT_MS) {
            adMob.show(activity)
        } ?: AdResult.TimedOut
    }
}
