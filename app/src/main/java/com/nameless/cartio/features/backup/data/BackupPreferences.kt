package com.nameless.cartio.features.backup.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

interface BackupPreferences {
    var isBackupEnabled: Boolean
}

@Singleton
class BackupPreferencesImpl @Inject constructor(
    @ApplicationContext context: Context
) : BackupPreferences {

    private val prefs = context.getSharedPreferences("backup_prefs", Context.MODE_PRIVATE)

    override var isBackupEnabled: Boolean
        get() = prefs.getBoolean(KEY_BACKUP_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BACKUP_ENABLED, value).apply()

    companion object {
        private const val KEY_BACKUP_ENABLED = "backup_enabled"
    }
}
