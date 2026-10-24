package com.minicore.cartio.features.shopping.ui

sealed class ScanTagEvent {
    data class TextSelected(val text: String) : ScanTagEvent()
}
