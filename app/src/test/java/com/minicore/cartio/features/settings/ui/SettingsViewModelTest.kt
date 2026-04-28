package com.minicore.cartio.features.settings.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import android.app.Activity
import com.minicore.cartio.features.backup.data.BackupPreferences
import com.minicore.cartio.features.backup.domain.CartioBackupManager
import com.minicore.cartio.features.monetization.domain.BillingRepository
import com.minicore.cartio.features.monetization.domain.HasAdFreeEntitlementUseCase
import com.minicore.cartio.features.settings.domain.ClearAllData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(
        clearAllData: ClearAllData = ClearAllData {},
        backupManager: CartioBackupManager = FakeBackupManager(),
        backupPreferences: BackupPreferences = FakeBackupPreferences(),
        fakeBillingRepository: BillingRepository = FakeBillingRepository()
    ) = SettingsViewModel(
        clearAllData,
        backupManager,
        backupPreferences,
        HasAdFreeEntitlementUseCase(fakeBillingRepository),
        fakeBillingRepository
    )

    // region sync toggle

    @Test
    fun `sync is disabled by default when preference is off`() {
        val viewModel = createViewModel(backupPreferences = FakeBackupPreferences(initial = false))
        assertFalse(viewModel.syncEnabled.value)
    }

    @Test
    fun `sync is enabled by default when preference is on`() {
        val viewModel = createViewModel(backupPreferences = FakeBackupPreferences(initial = true))
        assertTrue(viewModel.syncEnabled.value)
    }

    @Test
    fun `toggleSync true calls requestBackup and persists preference`() {
        val manager = FakeBackupManager()
        val prefs = FakeBackupPreferences()
        val viewModel = createViewModel(backupManager = manager, backupPreferences = prefs)

        viewModel.toggleSync(true)

        assertTrue(viewModel.syncEnabled.value)
        assertTrue(prefs.isBackupEnabled)
        assertTrue(manager.requestBackupCalled)
    }

    @Test
    fun `toggleSync false does not call requestBackup but persists preference`() {
        val manager = FakeBackupManager()
        val prefs = FakeBackupPreferences(initial = true)
        val viewModel = createViewModel(backupManager = manager, backupPreferences = prefs)

        viewModel.toggleSync(false)

        assertFalse(viewModel.syncEnabled.value)
        assertFalse(prefs.isBackupEnabled)
        assertFalse(manager.requestBackupCalled)
    }

    // endregion

    // region clear data dialog

    @Test
    fun `showClearDialog is false by default`() {
        val viewModel = createViewModel()
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `requestClearData shows dialog`() {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        assertTrue(viewModel.showClearDialog.value)
    }

    @Test
    fun `dismissClearDialog hides dialog`() {
        val viewModel = createViewModel()
        viewModel.requestClearData()
        viewModel.dismissClearDialog()
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `confirmClearData calls use case and hides dialog`() = runTest {
        var cleared = false
        val viewModel = createViewModel(clearAllData = ClearAllData { cleared = true })

        viewModel.requestClearData()
        viewModel.confirmClearData()
        advanceUntilIdle()

        assertTrue(cleared)
        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `confirmClearData hides dialog even if use case throws`() = runTest {
        val viewModel = createViewModel(clearAllData = ClearAllData { error("db error") })

        viewModel.requestClearData()
        viewModel.confirmClearData()
        advanceUntilIdle()

        assertFalse(viewModel.showClearDialog.value)
    }

    @Test
    fun `confirmClearData sets clearDataError on failure`() = runTest {
        val viewModel = createViewModel(clearAllData = ClearAllData { error("db error") })

        viewModel.confirmClearData()
        advanceUntilIdle()

        assertTrue(viewModel.clearDataError.value)
    }

    @Test
    fun `confirmClearData does not set clearDataError on success`() = runTest {
        val viewModel = createViewModel(clearAllData = ClearAllData {})

        viewModel.confirmClearData()
        advanceUntilIdle()

        assertFalse(viewModel.clearDataError.value)
    }

    @Test
    fun `dismissClearDataError clears error state`() = runTest {
        val viewModel = createViewModel(clearAllData = ClearAllData { error("db error") })

        viewModel.confirmClearData()
        advanceUntilIdle()
        viewModel.dismissClearDataError()

        assertFalse(viewModel.clearDataError.value)
    }

    // endregion

    // region billing tests

    @Test
    fun `adFreeEntitlement is false by default`() = runTest {
        val viewModel = createViewModel(fakeBillingRepository = FakeBillingRepository(entitlement = false))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.adFreeEntitlement.collect {}
        }
        advanceUntilIdle()
        assertFalse(viewModel.adFreeEntitlement.value)
    }

    @Test
    fun `adFreeEntitlement is true when user owns product`() = runTest {
        val viewModel = createViewModel(fakeBillingRepository = FakeBillingRepository(entitlement = true))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.adFreeEntitlement.collect {}
        }
        advanceUntilIdle()
        assertTrue(viewModel.adFreeEntitlement.value)
    }

    @Test
    fun `onBuyRemoveAdsClicked delegates to billing repository`() = runTest {
        val billing = FakeBillingRepository()
        val viewModel = createViewModel(fakeBillingRepository = billing)

        viewModel.onBuyRemoveAdsClicked(FakeActivity())
        advanceUntilIdle()

        assertTrue(billing.launchPurchaseCalled)
    }

    // endregion

    // region fakes

    private class FakeBackupManager : CartioBackupManager {
        var requestBackupCalled = false
        override fun requestBackup() { requestBackupCalled = true }
    }

    private class FakeBackupPreferences(initial: Boolean = false) : BackupPreferences {
        override var isBackupEnabled: Boolean = initial
    }

    private class FakeBillingRepository(private val entitlement: Boolean = false) : BillingRepository {
        var launchPurchaseCalled = false
        override val adFreeEntitlement: Flow<Boolean> = flowOf(entitlement)
        override suspend fun connect(): Boolean = true
        override suspend fun refreshEntitlements() {}
        override suspend fun launchRemoveAdsPurchase(activity: Activity) { launchPurchaseCalled = true }
    }

    private class FakeActivity : Activity()

    // endregion
}
