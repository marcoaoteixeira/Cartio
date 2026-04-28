package com.minicore.cartio.features.settings.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.minicore.cartio.features.backup.data.BackupPreferences
import com.minicore.cartio.features.backup.domain.CartioBackupManager
import com.minicore.cartio.features.monetization.domain.BillingRepository
import com.minicore.cartio.features.monetization.domain.HasAdFreeEntitlementUseCase
import com.minicore.cartio.features.settings.domain.ClearAllData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
// Direct re-export of BackupPreferences.backupEnabled (already a StateFlow).
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val clearAllData: ClearAllData,
    private val backupManager: CartioBackupManager,
    private val backupPreferences: BackupPreferences,
    private val hasAdFreeEntitlement: HasAdFreeEntitlementUseCase,
    private val billingRepository: BillingRepository
) : ViewModel() {

    val adFreeEntitlement: StateFlow<Boolean> = hasAdFreeEntitlement()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    val syncEnabled: StateFlow<Boolean> = backupPreferences.backupEnabled

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private val _clearDataError = MutableStateFlow(false)
    val clearDataError: StateFlow<Boolean> = _clearDataError.asStateFlow()

    fun toggleSync(enabled: Boolean) {
        backupPreferences.isBackupEnabled = enabled
        if (enabled) backupManager.requestBackup()
    }

    fun requestClearData() {
        _showClearDialog.value = true
    }

    fun dismissClearDialog() {
        _showClearDialog.value = false
    }

    fun confirmClearData() {
        viewModelScope.launch {
            val result = runCatching { clearAllData() }
            _clearDataError.value = result.isFailure
            _showClearDialog.value = false
        }
    }

    fun dismissClearDataError() {
        _clearDataError.value = false
    }

    fun onBuyRemoveAdsClicked(activity: Activity) {
        viewModelScope.launch { billingRepository.launchRemoveAdsPurchase(activity) }
    }
}
