package com.minicore.cartio.features.monetization

import com.minicore.cartio.BuildConfig

object MonetizationConfig {
    const val ADMOB_INTERSTITIAL_UNIT_ID = "ca-app-pub-8915922220503415/8512844639"
    const val ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS = "ca-app-pub-3940256099942544/1033173712"
    const val REMOVE_ADS_PRODUCT_ID = "cartio_buy_me_a_coffee_support"
    const val FREQUENCY_CAP_MINUTES = 30L
    const val AD_TIMEOUT_MS = 3_000L

    fun interstitialUnitId(debug: Boolean = BuildConfig.DEBUG): String =
        if (debug) ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS else ADMOB_INTERSTITIAL_UNIT_ID
}
