package com.minicore.cartio.features.monetization.data

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.minicore.cartio.features.monetization.MonetizationConfig
import com.minicore.cartio.features.monetization.domain.AdResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume

class AdMobDataSource @Inject constructor() {

    @Volatile private var interstitial: InterstitialAd? = null

    suspend fun preload(context: Context) = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            InterstitialAd.load(
                context,
                MonetizationConfig.interstitialUnitId(),
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        interstitial = ad
                        continuation.resume(Unit)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        interstitial = null
                        continuation.resume(Unit)
                    }
                }
            )
        }
    }

    fun hasAd(): Boolean = interstitial != null

    suspend fun show(activity: Activity): AdResult {
        val ad = interstitial ?: return AdResult.NotAvailable
        return suspendCancellableCoroutine { continuation ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitial = null
                    continuation.resume(AdResult.Shown)
                }

                override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                    interstitial = null
                    continuation.resume(AdResult.NotAvailable)
                }
            }
            ad.show(activity)
        }
    }
}
