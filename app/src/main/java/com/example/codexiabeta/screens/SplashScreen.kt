package com.example.codexiabeta.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.codexiabeta.R
import kotlinx.coroutines.delay

import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.codexiabeta.CodexiaApplication

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as CodexiaApplication
    val onboardingCompleted by app.userPreferences.onboardingCompleted.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(key1 = onboardingCompleted) {
        delay(1500L) // Wait for 1.5 seconds
        if (onboardingCompleted) {
            navController.navigate("main_screen") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("onboarding") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_codexia_logo),

            contentDescription = "Codexia App Logo",

            modifier = Modifier.size(192.dp)
        )
    }
}