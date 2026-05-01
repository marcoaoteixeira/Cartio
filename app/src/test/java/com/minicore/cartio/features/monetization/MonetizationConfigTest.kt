package com.minicore.cartio.features.monetization

import org.junit.Assert.assertEquals
import org.junit.Test

class MonetizationConfigTest {

    @Test
    fun `interstitialUnitId returns test ID when debug is true`() {
        assertEquals(
            MonetizationConfig.ADMOB_INTERSTITIAL_UNIT_ID_FOR_TESTS,
            MonetizationConfig.interstitialUnitId(debug = true)
        )
    }

    @Test
    fun `interstitialUnitId returns production ID when debug is false`() {
        assertEquals(
            MonetizationConfig.ADMOB_INTERSTITIAL_UNIT_ID,
            MonetizationConfig.interstitialUnitId(debug = false)
        )
    }
}
