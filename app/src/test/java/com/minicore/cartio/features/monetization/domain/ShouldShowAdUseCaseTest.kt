package com.minicore.cartio.features.monetization.domain

import android.app.Activity
import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ShouldShowAdUseCaseTest {

    @Test
    fun `premium user never sees ad`() = runTest {
        val useCase = ShouldShowAdUseCase(
            adsRepository = FakeAdsRepository(shouldShow = true),
            billingRepository = FakeBillingRepository(entitlement = true)
        )
        assertFalse(useCase())
    }

    @Test
    fun `free user within frequency cap does not see ad`() = runTest {
        val useCase = ShouldShowAdUseCase(
            adsRepository = FakeAdsRepository(shouldShow = false),
            billingRepository = FakeBillingRepository(entitlement = false)
        )
        assertFalse(useCase())
    }

    @Test
    fun `free user past frequency cap sees ad`() = runTest {
        val useCase = ShouldShowAdUseCase(
            adsRepository = FakeAdsRepository(shouldShow = true),
            billingRepository = FakeBillingRepository(entitlement = false)
        )
        assertTrue(useCase())
    }
}

class ShowDetailAdUseCaseTest {

    @Test
    fun `premium user gets SkippedForEntitlement`() = runTest {
        val useCase = ShowDetailAdUseCase(
            shouldShowAd = ShouldShowAdUseCase(
                adsRepository = FakeAdsRepository(shouldShow = true),
                billingRepository = FakeBillingRepository(entitlement = true)
            ),
            adsRepository = FakeAdsRepository(shouldShow = true)
        )
        val result = useCase(FakeActivity())
        assertTrue(result is AdResult.SkippedForEntitlement)
    }

    @Test
    fun `ad shown returns Shown and marks shown`() = runTest {
        val fakeAds = FakeAdsRepository(shouldShow = true, adResult = AdResult.Shown)
        val useCase = ShowDetailAdUseCase(
            shouldShowAd = ShouldShowAdUseCase(
                adsRepository = fakeAds,
                billingRepository = FakeBillingRepository(entitlement = false)
            ),
            adsRepository = fakeAds
        )
        val result = useCase(FakeActivity())
        assertTrue(result is AdResult.Shown)
        assertTrue(fakeAds.markAdShownCalled)
    }

    @Test
    fun `timeout result does not mark shown`() = runTest {
        val fakeAds = FakeAdsRepository(shouldShow = true, adResult = AdResult.TimedOut)
        val useCase = ShowDetailAdUseCase(
            shouldShowAd = ShouldShowAdUseCase(
                adsRepository = fakeAds,
                billingRepository = FakeBillingRepository(entitlement = false)
            ),
            adsRepository = fakeAds
        )
        val result = useCase(FakeActivity())
        assertTrue(result is AdResult.TimedOut)
        assertFalse(fakeAds.markAdShownCalled)
    }

    @Test
    fun `not available result does not mark shown`() = runTest {
        val fakeAds = FakeAdsRepository(shouldShow = true, adResult = AdResult.NotAvailable)
        val useCase = ShowDetailAdUseCase(
            shouldShowAd = ShouldShowAdUseCase(
                adsRepository = fakeAds,
                billingRepository = FakeBillingRepository(entitlement = false)
            ),
            adsRepository = fakeAds
        )
        val result = useCase(FakeActivity())
        assertTrue(result is AdResult.NotAvailable)
        assertFalse(fakeAds.markAdShownCalled)
    }
}

// region fakes

class FakeAdsRepository(
    private val shouldShow: Boolean = false,
    private val adResult: AdResult = AdResult.NotAvailable
) : AdsRepository {
    var markAdShownCalled = false

    override suspend fun preload(context: Context) {}
    override fun isAdAvailable(): Boolean = true
    override suspend fun showInterstitial(activity: Activity): AdResult = adResult
    override suspend fun shouldShowAd(): Boolean = shouldShow
    override suspend fun markAdShown() { markAdShownCalled = true }
}

class FakeBillingRepository(private val entitlement: Boolean = false) : BillingRepository {
    override val adFreeEntitlement: Flow<Boolean> = flowOf(entitlement)
    override suspend fun connect(): Boolean = true
    override suspend fun refreshEntitlements() {}
    override suspend fun launchRemoveAdsPurchase(activity: Activity) {}
}

class FakeActivity : Activity()

// endregion
