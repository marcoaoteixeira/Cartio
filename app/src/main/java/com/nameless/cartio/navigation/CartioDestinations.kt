package com.nameless.cartio.navigation

sealed class CartioDestinations(val route: String) {
    data object Shopping : CartioDestinations("shopping")
    data object Reports : CartioDestinations("reports")
    data object Settings : CartioDestinations("settings")
    data object ShoppingListDetail : CartioDestinations("shopping_list/{listId}") {
        const val ARG_LIST_ID = "listId"
        fun routeFor(listId: Long) = "shopping_list/$listId"
    }
    data object RegisterExpenses : CartioDestinations("register_expenses/{listId}") {
        const val ARG_LIST_ID = "listId"
        fun routeFor(listId: Long) = "register_expenses/$listId"
    }
}
