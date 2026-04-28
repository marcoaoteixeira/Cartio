package com.minicore.cartio.features.monetization.domain

import android.app.Activity
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val adFreeEntitlement: Flow<Boolean>
    suspend fun connect(): Boolean
    suspend fun refreshEntitlements()
    suspend fun launchRemoveAdsPurchase(activity: Activity)
}
