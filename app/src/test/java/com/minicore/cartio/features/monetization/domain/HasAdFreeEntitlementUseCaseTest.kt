package com.minicore.cartio.features.monetization.domain

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HasAdFreeEntitlementUseCaseTest {

    @Test
    fun `invoke returns false when billing reports no entitlement`() = runTest {
        val useCase = HasAdFreeEntitlementUseCase(FakeEntitlementBillingRepository(adFree = false))

        assertFalse(useCase().first())
    }

    @Test
    fun `invoke returns true when billing reports ad-free entitlement`() = runTest {
        val useCase = HasAdFreeEntitlementUseCase(FakeEntitlementBillingRepository(adFree = true))

        assertTrue(useCase().first())
    }

    @Test
    fun `invoke delegates directly to billing repository flow`() = runTest {
        val repo = FakeEntitlementBillingRepository(adFree = true)
        val useCase = HasAdFreeEntitlementUseCase(repo)

        val result = useCase()

        assertTrue(result === repo.adFreeEntitlement)
    }
}

private class FakeEntitlementBillingRepository(adFree: Boolean) : BillingRepository {
    override val adFreeEntitlement: Flow<Boolean> = flowOf(adFree)
    override suspend fun connect(): Boolean = true
    override suspend fun refreshEntitlements() {}
    override suspend fun launchRemoveAdsPurchase(activity: Activity) {}
}
