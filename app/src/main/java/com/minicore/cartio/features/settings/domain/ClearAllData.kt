package com.minicore.cartio.features.settings.domain

fun interface ClearAllData {
    suspend operator fun invoke()
}
