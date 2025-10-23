// In AppNavigation.kt

package com.example.codexiabeta

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.codexiabeta.screens.*

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "library") {
        composable("library") { LibraryScreen(navController) }
        composable("seriesDetail/{seriesId}") { backStackEntry ->
            // Dummy ID for now
            val seriesId = backStackEntry.arguments?.getString("seriesId") ?: "1"
            SeriesDetailScreen(navController, seriesId)
        }
        composable("insights") { InsightsScreen(navController) }
        composable("addEditSeries") { AddEditSeriesScreen(navController) }
    }
}