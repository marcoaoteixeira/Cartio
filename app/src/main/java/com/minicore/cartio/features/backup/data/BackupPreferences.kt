package com.minicore.cartio.features.backup.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

interface BackupPreferences {
    var isBackupEnabled: Boolean
    val backupEnabled: StateFlow<Boolean>
}

@Singleton
class BackupPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : BackupPreferences {

    private val prefs = context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    private val state = MutableStateFlow(prefs.getBoolean(KEY_BACKUP_ENABLED, false))

    override var isBackupEnabled: Boolean
        get() = state.value
        set(value) {
            state.value = value
            prefs.edit { putBoolean(KEY_BACKUP_ENABLED, value) }
        }

    override val backupEnabled: StateFlow<Boolean> = state.asStateFlow()

    companion object {
        const val PREFS_FILE_NAME = "backup_prefs"
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
    }
}
