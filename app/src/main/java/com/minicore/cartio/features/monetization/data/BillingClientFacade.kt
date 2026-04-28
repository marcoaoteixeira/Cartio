package com.minicore.cartio.features.monetization.data

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Subset of [com.android.billingclient.api.BillingClient] surface that
 * [BillingClientWrapper] actually uses. Allows fakes in unit tests.
 */
interface BillingClientFacade {
    fun startConnection(listener: BillingClientStateListener)
    fun queryPurchasesAsync(params: QueryPurchasesParams, listener: PurchasesResponseListener)
    fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        listener: ProductDetailsResponseListener
    )
    fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult
    fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        listener: com.android.billingclient.api.AcknowledgePurchaseResponseListener
    )
}
