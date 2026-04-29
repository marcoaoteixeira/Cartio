package com.minicore.cartio.features.monetization.data

import android.app.Activity
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.AcknowledgePurchaseResponseListener
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.QueryPurchasesParams

/**
 * Test double for [BillingClientFacade].
 *
 * Each interaction is recorded for assertions, and connection / query results
 * can be configured before the wrapper is exercised.
 */
class FakeBillingClientFacade : BillingClientFacade {

    var connectionResponseCode: Int = BillingClient.BillingResponseCode.OK
    var purchasesToReturn: List<Purchase> = emptyList()
    var productDetailsResultBuilder: () -> QueryProductDetailsResult? = { null }

    var startConnectionInvocations: Int = 0
        private set
    val acknowledgedTokens = mutableListOf<String>()
    var launchBillingFlowInvocations: Int = 0
        private set

    override fun startConnection(listener: BillingClientStateListener) {
        startConnectionInvocations++
        listener.onBillingSetupFinished(billingResult(connectionResponseCode))
    }

    override fun queryPurchasesAsync(params: QueryPurchasesParams, listener: PurchasesResponseListener) {
        listener.onQueryPurchasesResponse(billingResult(BillingClient.BillingResponseCode.OK), purchasesToReturn)
    }

    override fun queryProductDetailsAsync(
        params: QueryProductDetailsParams,
        listener: ProductDetailsResponseListener
    ) {
        val result = productDetailsResultBuilder()
        if (result != null) {
            listener.onProductDetailsResponse(billingResult(BillingClient.BillingResponseCode.OK), result)
        }
    }

    override fun launchBillingFlow(activity: Activity, params: BillingFlowParams): BillingResult {
        launchBillingFlowInvocations++
        return billingResult(BillingClient.BillingResponseCode.OK)
    }

    override fun acknowledgePurchase(
        params: AcknowledgePurchaseParams,
        listener: AcknowledgePurchaseResponseListener
    ) {
        acknowledgedTokens.add(params.purchaseToken)
        listener.onAcknowledgePurchaseResponse(billingResult(BillingClient.BillingResponseCode.OK))
    }

    private fun billingResult(code: Int): BillingResult =
        BillingResult.newBuilder().setResponseCode(code).build()
}
