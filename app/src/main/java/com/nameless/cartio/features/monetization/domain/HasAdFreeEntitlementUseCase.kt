package com.nameless.cartio.features.monetization.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HasAdFreeEntitlementUseCase @Inject constructor(
    private val billingRepository: BillingRepository
) {
    operator fun invoke(): Flow<Boolean> = billingRepository.adFreeEntitlement
}
