package com.example.codexiabeta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.codexiabeta.screens.*
import com.example.codexiabeta.ui.theme.CodexiaBetaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This function links the theme to the activity and must be called before setContent
        installSplashScreen()

        setContent {
            CodexiaBetaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "splash") {
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }
                        composable("main_screen") {
                            MainScreen(mainNavController = navController)
                        }
                        composable("onboarding") {
                            OnboardingScreen(
                                onFinish = {
                                    navController.navigate("main_screen") {
                                        popUpTo("onboarding") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("seriesDetail/{seriesId}") { backStackEntry ->
                            val seriesId = backStackEntry.arguments?.getString("seriesId")
                            SeriesDetailScreen(navController = navController, seriesId = seriesId ?: "1")
                        }
                        composable(
                            route = "addEditSeries/{seriesId}",
                            arguments = listOf(navArgument("seriesId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            })
                        ) { backStackEntry ->
                            val seriesId = backStackEntry.arguments?.getString("seriesId")
                            AddEditSeriesScreen(
                                navController = navController,
                                seriesId = seriesId
                            )
                        }
                        composable("profile") {
                            ProfileScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}