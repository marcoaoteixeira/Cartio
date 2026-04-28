package com.minicore.cartio.features.monetization.data

import com.android.billingclient.api.BillingClient
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Wrapper tests covering the surface that does not require constructing real
 * [com.android.billingclient.api.Purchase] instances. Constructing those goes
 * through `org.json.JSONObject`, which is unavailable in plain JVM tests
 * (would require Robolectric — an explicit non-goal of this feature).
 *
 * Coverage here is the dispatch contract between wrapper and facade plus the
 * empty / unrelated-purchase paths through `refreshEntitlements`.
 */
class BillingClientWrapperTest {

    private val facade = FakeBillingClientFacade()
    private val wrapper = BillingClientWrapper(facade)

    @Test
    fun `connect returns true when billing setup succeeds`() = runTest {
        facade.connectionResponseCode = BillingClient.BillingResponseCode.OK

        val result = wrapper.connect()

        assertTrue(result)
        assertEquals(1, facade.startConnectionInvocations)
    }

    @Test
    fun `connect returns false on non-OK setup`() = runTest {
        facade.connectionResponseCode = BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE

        assertFalse(wrapper.connect())
    }

    @Test
    fun `refreshEntitlements stays false when there are no purchases`() = runTest {
        facade.purchasesToReturn = emptyList()

        wrapper.refreshEntitlements()

        assertFalse(wrapper.adFreeEntitlement.first())
    }
}
