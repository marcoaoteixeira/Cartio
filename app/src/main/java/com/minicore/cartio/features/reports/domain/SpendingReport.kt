package com.minicore.cartio.features.reports.domain

data class SpendingReport(
    val totalSpent: Double,
    val topItems: List<ItemSpending>
)

data class ItemSpending(
    val productName: String,
    val totalSpent: Double,
    val purchaseCount: Int
)
