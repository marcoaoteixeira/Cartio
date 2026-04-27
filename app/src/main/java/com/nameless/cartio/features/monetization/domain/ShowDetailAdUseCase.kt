package com.nameless.cartio.features.monetization.domain

import android.app.Activity
import javax.inject.Inject

class ShowDetailAdUseCase @Inject constructor(
    private val shouldShowAd: ShouldShowAdUseCase,
    private val adsRepository: AdsRepository
) {
    suspend operator fun invoke(activity: Activity): AdResult {
        if (!shouldShowAd()) return AdResult.SkippedForEntitlement
        val result = adsRepository.showInterstitial(activity)
        if (result is AdResult.Shown) adsRepository.markAdShown()
        return result
    }
}
