package com.nameless.cartio.navigation

sealed class CartioDestinations(val route: String) {
    data object Shopping : CartioDestinations("shopping")
    data object Reports : CartioDestinations("reports")
    data object Settings : CartioDestinations("settings")
}
