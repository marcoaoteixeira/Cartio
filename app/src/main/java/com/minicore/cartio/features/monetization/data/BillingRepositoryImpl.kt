package com.minicore.cartio.features.monetization.data

import android.app.Activity
import com.minicore.cartio.features.monetization.domain.BillingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BillingRepositoryImpl @Inject constructor(
    private val wrapper: BillingClientWrapper
) : BillingRepository {

    override val adFreeEntitlement: Flow<Boolean> = wrapper.adFreeEntitlement

    override suspend fun connect(): Boolean = wrapper.connect()

    override suspend fun refreshEntitlements() = wrapper.refreshEntitlements()

    override suspend fun launchRemoveAdsPurchase(activity: Activity) = wrapper.launchPurchase(activity)
}
