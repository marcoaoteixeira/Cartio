package com.minicore.cartio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.lifecycleScope
import com.minicore.cartio.R
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.minicore.cartio.BuildConfig
import com.minicore.cartio.core.ui.theme.Alpha
import com.minicore.cartio.core.ui.theme.CartioTheme
import com.minicore.cartio.features.monetization.domain.BillingRepository
import com.minicore.cartio.features.splash.ui.CartioSplashScreen
import com.minicore.cartio.navigation.CartioDestinations
import com.minicore.cartio.navigation.CartioNavGraph
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var billingRepository: BillingRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                lifecycleScope.launch { billingRepository.refreshEntitlements() }
            }
        })
        setContent {
            CartioTheme {
                var splashCompleted by rememberSaveable { mutableStateOf(false) }

                if (!splashCompleted) {
                    CartioSplashScreen(onComplete = { splashCompleted = true })
                } else {
                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentRoute = navBackStackEntry?.destination?.route
                    val drawerState = rememberDrawerState(DrawerValue.Closed)
                    val scope = rememberCoroutineScope()

                    val navItems = listOf(
                        NavDrawerItem(CartioDestinations.Shopping, Icons.Rounded.ShoppingCart, stringResource(R.string.nav_item_shopping), stringResource(R.string.nav_item_shopping_subtitle)),
                        NavDrawerItem(CartioDestinations.Reports, Icons.Rounded.BarChart, stringResource(R.string.nav_item_reports), stringResource(R.string.nav_item_reports_subtitle)),
                        NavDrawerItem(CartioDestinations.Settings, Icons.Rounded.Settings, stringResource(R.string.nav_item_settings), stringResource(R.string.nav_item_settings_subtitle)),
                    )

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.primary)
                                        .statusBarsPadding()
                                        .padding(24.dp)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = Alpha.Subtle)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Rounded.ShoppingCart,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = stringResource(R.string.app_name),
                                            style = MaterialTheme.typography.headlineSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                        Text(
                                            text = stringResource(R.string.nav_drawer_tagline),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = Alpha.Secondary)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f).padding(vertical = 12.dp)) {
                                    navItems.forEach { item ->
                                        DrawerNavItem(
                                            item = item,
                                            selected = currentRoute == item.destination.route,
                                            primaryColor = MaterialTheme.colorScheme.primary,
                                            onSurfaceColor = MaterialTheme.colorScheme.onSurface,
                                            outlineColor = MaterialTheme.colorScheme.outline,
                                            onClick = {
                                                scope.launch { drawerState.close() }
                                                navController.navigate(item.destination.route) {
                                                    popUpTo(CartioDestinations.Shopping.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        )
                                    }
                                }

                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Text(
                                    text = stringResource(R.string.app_version_footer, BuildConfig.VERSION_NAME),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier
                                        .padding(horizontal = 24.dp, vertical = 16.dp)
                                        .navigationBarsPadding()
                                )
                            }
                        }
                    ) {
                        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                            CartioNavGraph(
                                navController = navController,
                                innerPadding = innerPadding,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class NavDrawerItem(
    val destination: CartioDestinations,
    val icon: ImageVector,
    val label: String,
    val subtitle: String
)

@Composable
private fun DrawerNavItem(
    item: NavDrawerItem,
    selected: Boolean,
    primaryColor: Color,
    onSurfaceColor: Color,
    outlineColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) primaryColor.copy(alpha = Alpha.Selected) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(primaryColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
        } else {
            Spacer(modifier = Modifier.width(15.dp))
        }
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = if (selected) primaryColor else outlineColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) primaryColor else onSurfaceColor
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = outlineColor
            )
        }
    }
}
