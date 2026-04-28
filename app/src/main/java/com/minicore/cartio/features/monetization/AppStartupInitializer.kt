package com.minicore.cartio.features.monetization

import android.content.Context
import com.minicore.cartio.features.monetization.domain.AdsRepository
import com.minicore.cartio.features.monetization.domain.BillingRepository
import javax.inject.Inject

class AppStartupInitializer @Inject constructor(
    private val billingRepository: BillingRepository,
    private val adsRepository: AdsRepository
) {
    suspend fun initialize(context: Context) {
        val connected = billingRepository.connect()
        if (connected) billingRepository.refreshEntitlements()
        adsRepository.preload(context)
    }
}
