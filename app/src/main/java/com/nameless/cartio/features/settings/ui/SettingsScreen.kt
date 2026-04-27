package com.nameless.cartio.features.settings.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nameless.cartio.BuildConfig
import com.nameless.cartio.R
import com.nameless.cartio.core.config.AppConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    onOpenDrawer: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as Activity
    val syncEnabled by viewModel.syncEnabled.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearDialog.collectAsStateWithLifecycle()
    val clearDataError by viewModel.clearDataError.collectAsStateWithLifecycle()
    val adFreeEntitlement by viewModel.adFreeEntitlement.collectAsStateWithLifecycle()

    if (showClearDialog) {
        ClearDataDialog(
            onConfirm = viewModel::confirmClearData,
            onDismiss = viewModel::dismissClearDialog
        )
    }

    if (clearDataError) {
        ClearDataErrorDialog(onDismiss = viewModel::dismissClearDataError)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            Icons.Rounded.Menu,
                            contentDescription = stringResource(R.string.nav_open_menu),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                title = {
                    Text(
                        text = stringResource(R.string.settings_screen_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.background
    ) { scaffoldPadding ->
        val combinedPadding = PaddingValues(
            top = scaffoldPadding.calculateTopPadding(),
            bottom = innerPadding.calculateBottomPadding(),
            start = 16.dp,
            end = 16.dp
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = combinedPadding
        ) {
            item { Spacer(modifier = Modifier.height(12.dp)) }

            item {
                SettingsSection(label = stringResource(R.string.settings_section_backup)) {
                    ToggleSettingItem(
                        icon = {
                            SettingsIconBox(backgroundColor = MaterialTheme.colorScheme.primary) {
                                Icon(
                                    Icons.Rounded.Sync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = stringResource(R.string.settings_backup_title),
                        subtitle = if (syncEnabled) stringResource(R.string.settings_backup_subtitle_on)
                                   else stringResource(R.string.settings_backup_subtitle_off),
                        checked = syncEnabled,
                        onCheckedChange = viewModel::toggleSync
                    )
                }
            }

            item {
                Text(
                    text = stringResource(R.string.settings_backup_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                if (adFreeEntitlement) {
                    PurchasedCard()
                } else {
                    PromoCard(onBuyClick = { viewModel.onBuyRemoveAdsClicked(activity) })
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSection(label = stringResource(R.string.settings_section_data)) {
                    SettingsListItem(
                        icon = {
                            SettingsIconBox(backgroundColor = MaterialTheme.colorScheme.error) {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = stringResource(R.string.settings_clear_data_title),
                        subtitle = stringResource(R.string.settings_clear_data_subtitle),
                        titleColor = MaterialTheme.colorScheme.error,
                        subtitleColor = MaterialTheme.colorScheme.error,
                        showChevron = true,
                        onClick = viewModel::requestClearData
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSection(label = stringResource(R.string.settings_section_about)) {
                    SettingsListItem(
                        icon = {
                            SettingsIconBox(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Icon(
                                    Icons.Rounded.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = "Cartio",
                        subtitle = "Version ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})",
                        showDivider = true,
                        onClick = { openPlayStore(context) }
                    )
                    SettingsListItem(
                        icon = {
                            SettingsIconBox(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = stringResource(R.string.settings_licenses_title),
                        showChevron = true,
                        onClick = { /* TODO(CARTIO-OSS): open open-source licenses screen */ }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            item {
                Column(modifier = Modifier.fillParentMaxWidth()) {
                    Text(
                        text = "made with care · cartio v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .padding(bottom = 8.dp)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

private fun openPlayStore(context: Context) {
    val market = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=${context.packageName}")
    ).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        context.startActivity(market)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL)))
    }
}

@Composable
private fun ClearDataErrorDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_clear_error_title)) },
        text = { Text(stringResource(R.string.settings_clear_error_body)) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_ok)) }
        }
    )
}

@Composable
private fun ClearDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_clear_dialog_title)) },
        text = {
            Text(stringResource(R.string.settings_clear_dialog_body))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.settings_clear_dialog_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_clear_dialog_cancel)) }
        }
    )
}
