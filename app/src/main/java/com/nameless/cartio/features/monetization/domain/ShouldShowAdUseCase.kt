package com.nameless.cartio.features.monetization.domain

import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ShouldShowAdUseCase @Inject constructor(
    private val adsRepository: AdsRepository,
    private val billingRepository: BillingRepository
) {
    suspend operator fun invoke(): Boolean {
        if (billingRepository.adFreeEntitlement.first()) return false
        return adsRepository.shouldShowAd()
    }
}
