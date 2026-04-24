package com.nameless.cartio.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nameless.cartio.features.reports.ReportsScreen
import com.nameless.cartio.features.settings.SettingsScreen
import com.nameless.cartio.features.shopping.ui.ShoppingListScreen

@Composable
fun CartioNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = CartioDestinations.Shopping.route
    ) {
        composable(CartioDestinations.Shopping.route) {
            ShoppingListScreen(innerPadding = innerPadding)
        }
        composable(CartioDestinations.Reports.route) {
            ReportsScreen(innerPadding = innerPadding)
        }
        composable(CartioDestinations.Settings.route) {
            SettingsScreen(innerPadding = innerPadding)
        }
    }
}
