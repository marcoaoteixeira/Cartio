package com.nameless.cartio.features.settings.domain

fun interface ClearAllData {
    suspend operator fun invoke()
}
