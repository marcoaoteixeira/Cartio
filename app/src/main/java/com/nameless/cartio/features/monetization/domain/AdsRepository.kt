package com.nameless.cartio.features.monetization.domain

import android.app.Activity
import android.content.Context

interface AdsRepository {
    suspend fun preload(context: Context)
    fun isAdAvailable(): Boolean
    suspend fun showInterstitial(activity: Activity): AdResult
    suspend fun shouldShowAd(): Boolean
    suspend fun markAdShown()
}
