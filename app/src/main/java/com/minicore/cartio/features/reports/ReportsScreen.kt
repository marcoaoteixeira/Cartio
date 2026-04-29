package com.minicore.cartio.features.reports

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.minicore.cartio.R
import com.minicore.cartio.core.format.CurrencyFormat
import com.minicore.cartio.features.reports.domain.ItemSpending
import com.minicore.cartio.features.reports.domain.SpendingReport

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    innerPadding: PaddingValues,
    onOpenDrawer: () -> Unit = {},
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val report by viewModel.report.collectAsStateWithLifecycle()

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
                        text = stringResource(R.string.nav_item_reports),
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

        val currentReport = report
        if (currentReport == null || (currentReport.totalSpent == 0.0 && currentReport.topItems.isEmpty())) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(combinedPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.reports_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = combinedPadding,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                item { SpendingSummaryCard(report = currentReport) }
                if (currentReport.topItems.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.reports_most_bought),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    val maxSpent = currentReport.topItems.maxOfOrNull { it.totalSpent } ?: 1.0
                    items(currentReport.topItems) { item ->
                        TopItemRow(item = item, maxSpent = maxSpent)
                    }
                }
                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
private fun SpendingSummaryCard(report: SpendingReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.reports_last_30_days),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Use the M3 typography scale so font-scale settings affect this
            // headline; previously hard-coded sp ignored accessibility prefs.
            val wholeStyle = MaterialTheme.typography.displaySmall
            val centsStyle = MaterialTheme.typography.titleLarge
            val wholePart = report.totalSpent.toLong()
            val centsPart = ((report.totalSpent - wholePart) * 100).toLong()
            Text(
                text = buildAnnotatedString {
                    withStyle(wholeStyle.toSpanStyle().copy(fontWeight = FontWeight.Bold)) {
                        append("$$wholePart")
                    }
                    withStyle(centsStyle.toSpanStyle().copy(fontWeight = FontWeight.SemiBold)) {
                        append(".%02d".format(centsPart))
                    }
                },
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun TopItemRow(item: ItemSpending, maxSpent: Double) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.productName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = CurrencyFormat.format(item.totalSpent),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        LinearProgressIndicator(
            progress = { (item.totalSpent / maxSpent).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
        Text(
            text = stringResource(R.string.reports_bought_count, item.purchaseCount),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
