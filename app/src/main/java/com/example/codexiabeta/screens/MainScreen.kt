// In screens/MainScreen.kt

package com.example.codexiabeta.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.codexiabeta.navigation.NavigationItem

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(mainNavController: NavHostController) {
    val bottomBarNavController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomNavigationBar(navController = bottomBarNavController) }
    ) { innerPadding ->
        NavHost(
            navController = bottomBarNavController,
            // --- SET HOME AS THE NEW START DESTINATION ---
            startDestination = NavigationItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- ADD THE NEW HOME COMPOSABLE TO THE GRAPH ---
            composable(NavigationItem.Home.route) {
                HomeScreen(navController = mainNavController)
            }
            composable(NavigationItem.Library.route) {
                LibraryScreen(navController = mainNavController)
            }
            composable(NavigationItem.Insights.route) {
                InsightsScreen(navController = mainNavController)
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(navController: NavHostController) {
    // --- ADD HOME TO THE LIST OF ITEMS ---
    val navItems = listOf(NavigationItem.Home, NavigationItem.Library, NavigationItem.Insights)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navItems.forEach { item ->
            NavigationBarItem(
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true; restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}