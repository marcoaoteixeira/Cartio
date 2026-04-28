package com.minicore.cartio.features.backup.data

import android.app.backup.BackupManager
import android.content.Context
import com.minicore.cartio.features.backup.domain.CartioBackupManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CartioBackupManagerImpl @Inject constructor(
    @ApplicationContext context: Context
) : CartioBackupManager {

    private val backupManager = BackupManager(context)

    override fun requestBackup() {
        backupManager.dataChanged()
    }
}
