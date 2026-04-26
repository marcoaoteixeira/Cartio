package com.nameless.cartio.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nameless.cartio.features.reports.ReportsScreen
import com.nameless.cartio.features.settings.ui.SettingsScreen
import com.nameless.cartio.features.shopping.ui.ShoppingListDetailScreen
import com.nameless.cartio.features.shopping.ui.ShoppingListScreen

@Composable
fun CartioNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onOpenDrawer: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = CartioDestinations.Shopping.route
    ) {
        composable(CartioDestinations.Shopping.route) {
            ShoppingListScreen(
                innerPadding = innerPadding,
                onOpenDrawer = onOpenDrawer,
                onNavigateToDetail = { listId ->
                    navController.navigate(CartioDestinations.ShoppingListDetail.routeFor(listId))
                }
            )
        }
        composable(CartioDestinations.Reports.route) {
            ReportsScreen(innerPadding = innerPadding, onOpenDrawer = onOpenDrawer)
        }
        composable(CartioDestinations.Settings.route) {
            SettingsScreen(innerPadding = innerPadding, onOpenDrawer = onOpenDrawer)
        }
        composable(
            route = CartioDestinations.ShoppingListDetail.route,
            arguments = listOf(
                navArgument(CartioDestinations.ShoppingListDetail.ARG_LIST_ID) {
                    type = NavType.LongType
                }
            )
        ) {
            ShoppingListDetailScreen(
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}
