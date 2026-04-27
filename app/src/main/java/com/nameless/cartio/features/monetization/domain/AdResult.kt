package com.nameless.cartio.features.monetization.domain

sealed class AdResult {
    data object Shown : AdResult()
    data object NotAvailable : AdResult()
    data object TimedOut : AdResult()
    data object SkippedForEntitlement : AdResult()
}
