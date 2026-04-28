package com.minicore.cartio.features.monetization.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.QueryProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.minicore.cartio.features.monetization.MonetizationConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class BillingClientWrapper @Inject constructor(context: Context) {

    private val _adFreeEntitlement = MutableStateFlow(false)
    val adFreeEntitlement: StateFlow<Boolean> = _adFreeEntitlement.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK) {
            purchases?.forEach { handlePurchase(it) }
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
        )
        .setListener(purchasesUpdatedListener)
        .build()

    suspend fun connect(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                Log.d(TAG, "connect cancelled before billing setup completed")
            }
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(result: BillingResult) {
                    if (continuation.isActive) {
                        continuation.resume(result.responseCode == BillingClient.BillingResponseCode.OK)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // BillingClient handles reconnection on the next call automatically
                }
            })
        }
    }

    suspend fun refreshEntitlements() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                Log.d(TAG, "refreshEntitlements cancelled while query in flight")
            }
            billingClient.queryPurchasesAsync(params) { _, purchases ->
                val hasEntitlement = purchases.any { purchase ->
                    purchase.products.contains(MonetizationConfig.REMOVE_ADS_PRODUCT_ID) &&
                        purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                _adFreeEntitlement.value = hasEntitlement
                purchases.forEach { handlePurchase(it) }
                if (continuation.isActive) continuation.resume(Unit)
            }
        }
    }

    suspend fun launchPurchase(activity: Activity) {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(MonetizationConfig.REMOVE_ADS_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val detailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation {
                Log.d(TAG, "launchPurchase cancelled before product details returned")
            }
            billingClient.queryProductDetailsAsync(
                detailsParams,
                object : ProductDetailsResponseListener {
                    override fun onProductDetailsResponse(
                        billingResult: BillingResult,
                        result: QueryProductDetailsResult
                    ) {
                        val productDetails = result.productDetailsList.firstOrNull()
                        if (productDetails != null) {
                            val productDetailsParams = BillingFlowParams.ProductDetailsParams
                                .newBuilder()
                                .setProductDetails(productDetails)
                                .build()
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(listOf(productDetailsParams))
                                .build()
                            billingClient.launchBillingFlow(activity, billingFlowParams)
                        }
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                }
            )
        }
    }

    companion object {
        private const val TAG = "BillingClientWrapper"
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
            purchase.products.contains(MonetizationConfig.REMOVE_ADS_PRODUCT_ID)
        ) {
            _adFreeEntitlement.value = true
            if (!purchase.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                        Log.w(TAG, "Acknowledge failed (${result.responseCode}): ${result.debugMessage}")
                    }
                }
            }
        }
    }
}
