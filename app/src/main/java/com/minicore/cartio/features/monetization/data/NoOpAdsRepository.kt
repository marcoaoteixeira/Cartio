package com.minicore.cartio.features.monetization.data

import android.app.Activity
import android.content.Context
import com.minicore.cartio.features.monetization.domain.AdResult
import com.minicore.cartio.features.monetization.domain.AdsRepository

class NoOpAdsRepository : AdsRepository {
    override suspend fun preload(context: Context) = Unit
    override fun isAdAvailable(): Boolean = false
    override suspend fun showInterstitial(activity: Activity): AdResult = AdResult.NotAvailable
    override suspend fun shouldShowAd(): Boolean = false
    override suspend fun markAdShown() = Unit
}
