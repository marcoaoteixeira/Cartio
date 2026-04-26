package com.nameless.cartio.features.settings.ui

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
import androidx.compose.material.icons.rounded.Article
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nameless.cartio.BuildConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    innerPadding: PaddingValues,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val syncEnabled by viewModel.syncEnabled.collectAsStateWithLifecycle()
    val showClearDialog by viewModel.showClearDialog.collectAsStateWithLifecycle()

    if (showClearDialog) {
        ClearDataDialog(
            onConfirm = viewModel::confirmClearData,
            onDismiss = viewModel::dismissClearDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
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
                SettingsSection(label = "BACKUP & SYNC") {
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
                        title = "Sync with Google Drive",
                        subtitle = if (syncEnabled) "On · Syncing to Google Drive"
                                   else "Off · Stored only on this device",
                        checked = syncEnabled,
                        onCheckedChange = viewModel::toggleSync
                    )
                }
            }

            item {
                Text(
                    text = "Keep your lists safe across devices.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item { PromoCard(onBuyClick = {}) }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSection(label = "DATA") {
                    SettingsListItem(
                        icon = {
                            SettingsIconBox(backgroundColor = Color(0xFF37474F)) {
                                Icon(
                                    Icons.Rounded.Download,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = "Export lists as JSON",
                        subtitle = "Download a backup file",
                        showDivider = true,
                        showChevron = true,
                        onClick = {}
                    )
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
                        title = "Clear all data",
                        subtitle = "Removes every list, item and price history",
                        titleColor = MaterialTheme.colorScheme.error,
                        subtitleColor = MaterialTheme.colorScheme.error,
                        showChevron = true,
                        onClick = viewModel::requestClearData
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SettingsSection(label = "ABOUT") {
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
                        showDivider = true
                    )
                    SettingsListItem(
                        icon = {
                            SettingsIconBox(backgroundColor = MaterialTheme.colorScheme.surfaceVariant) {
                                Icon(
                                    Icons.Rounded.Article,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        title = "Open-source licenses",
                        showChevron = true,
                        onClick = {}
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

@Composable
private fun ClearDataDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Clear all data?") },
        text = {
            Text("This removes every list, item and price history permanently.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete All", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
