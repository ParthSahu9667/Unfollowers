package com.unfollowlens.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unfollowlens.ui.dashboard.DashboardScreen
import com.unfollowlens.ui.history.HistoryScreen
import com.unfollowlens.ui.lists.ListsScreen
import com.unfollowlens.ui.settings.SettingsScreen
import com.unfollowlens.ui.theme.AccentPrimary
import com.unfollowlens.ui.theme.BgSurfaceElevated
import com.unfollowlens.ui.theme.TextPrimary
import com.unfollowlens.ui.theme.TextSecondary

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Lists : Screen("lists?category={category}", "Lists", Icons.Default.People) {
        fun createRoute(category: String) = "lists?category=$category"
    }
    object History : Screen("history", "History", Icons.Default.History)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(Screen.Dashboard, Screen.Lists, Screen.History, Screen.Settings)

    Scaffold(
        containerColor = com.unfollowlens.ui.theme.BgBase,
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            NavigationBar(
                containerColor = BgSurfaceElevated,
                contentColor = TextPrimary
            ) {
                items.forEach { screen ->
                    // Base route extraction for checking selection (handles arguments)
                    val baseRoute = screen.route.substringBefore("?")
                    val isSelected = currentRoute?.startsWith(baseRoute) == true

                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            val route = if (screen == Screen.Lists) "lists?category=not_back" else screen.route
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = AccentPrimary,
                            selectedTextColor = AccentPrimary,
                            indicatorColor = AccentPrimary.copy(alpha = 0.2f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(300)) },
            exitTransition = { fadeOut(animationSpec = tween(300)) },
            popEnterTransition = { fadeIn(animationSpec = tween(300)) },
            popExitTransition = { fadeOut(animationSpec = tween(300)) }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToList = { category ->
                        navController.navigate(Screen.Lists.createRoute(category)) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(Screen.Lists.route) { backStackEntry ->
                val category = backStackEntry.arguments?.getString("category")
                ListsScreen(initialCategory = category)
            }
            composable(Screen.History.route) {
                HistoryScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
