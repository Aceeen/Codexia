// In navigation/AppNavigation.kt

package com.example.codexiabeta.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavigationItem(val route: String, val icon: ImageVector, val title: String) {
    object Home : NavigationItem("home", Icons.Default.Home, "Home")
    object Library : NavigationItem("library", Icons.Default.AutoStories, "Library")
    object Insights : NavigationItem("insights", Icons.Default.BarChart, "Insights")
}