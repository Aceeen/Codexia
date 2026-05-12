package com.example.codexiabeta.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codexiabeta.CodexiaApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as CodexiaApplication
    val scope = rememberCoroutineScope()

    var nameInput by remember { mutableStateOf("") }
    var step by remember { mutableIntStateOf(0) }

    // Animations
    val alpha1 by animateFloatAsState(targetValue = if (step >= 0) 1f else 0f, animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "alpha1")
    val alpha2 by animateFloatAsState(targetValue = if (step >= 1) 1f else 0f, animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "alpha2")
    val alpha3 by animateFloatAsState(targetValue = if (step >= 2) 1f else 0f, animationSpec = tween(1500, easing = FastOutSlowInEasing), label = "alpha3")

    LaunchedEffect(Unit) {
        delay(500)
        step = 1
        delay(1500)
        step = 2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "CODEXIA",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(alpha1)
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome, Chronicler.",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.alpha(alpha2)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To whom shall this Grimoire belong?",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(alpha2)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Enter your name") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(alpha3)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val finalName = nameInput.trim().ifBlank { "Reader" }
                    scope.launch {
                        app.userPreferences.setUserName(finalName)
                        app.userPreferences.setOnboardingCompleted(true)
                        onFinish()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .alpha(alpha3),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Awaken", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
