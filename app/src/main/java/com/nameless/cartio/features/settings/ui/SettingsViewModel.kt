package com.nameless.cartio.features.settings.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nameless.cartio.features.backup.data.BackupPreferences
import com.nameless.cartio.features.backup.domain.CartioBackupManager
import com.nameless.cartio.features.monetization.domain.BillingRepository
import com.nameless.cartio.features.monetization.domain.HasAdFreeEntitlementUseCase
import com.nameless.cartio.features.settings.domain.ClearAllData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
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

    private val _syncEnabled = MutableStateFlow(backupPreferences.isBackupEnabled)
    val syncEnabled: StateFlow<Boolean> = _syncEnabled.asStateFlow()

    private val _showClearDialog = MutableStateFlow(false)
    val showClearDialog: StateFlow<Boolean> = _showClearDialog.asStateFlow()

    private val _clearDataError = MutableStateFlow(false)
    val clearDataError: StateFlow<Boolean> = _clearDataError.asStateFlow()

    fun toggleSync(enabled: Boolean) {
        _syncEnabled.value = enabled
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
