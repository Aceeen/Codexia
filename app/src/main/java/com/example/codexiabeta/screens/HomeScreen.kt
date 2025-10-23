// In screens/HomeScreen.kt

package com.example.codexiabeta.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.codexiabeta.model.DummyData
import com.example.codexiabeta.model.LogEntry
import com.example.codexiabeta.model.Series
import com.example.codexiabeta.ui.theme.AccentOrange

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        // --- CHANGE: INCREASED SPACING BETWEEN ALL SECTIONS ---
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        GreetingSection()
        ContinueReadingSection(navController = navController)
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        RecentLogsSection(navController = navController)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingSection() {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Good Evening, Acin.",
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset(4f, 4f),
                    blurRadius = 8f
                )
            )
        )
    }
}


@Composable
private fun ContinueReadingSection(navController: NavController) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Continue Reading", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = { navController.navigate("library") }) {
                Text("View All")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            itemsIndexed(DummyData.seriesList) { index, series ->
                ContinueReadingCard(
                    series = series,
                    isEnlarged = index == 0,
                    onClick = { navController.navigate("seriesDetail/${series.id}") }
                )
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(series: Series, isEnlarged: Boolean, onClick: () -> Unit) {
    val scale = if (isEnlarged) 1.05f else 1.0f
    Card(
        modifier = Modifier
            .width(if (isEnlarged) 160.dp else 140.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Image(
                painter = painterResource(id = series.coverResId),
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 200f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = series.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ch. ${series.latestChapter}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RecentLogsSection(navController: NavController) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recent Logs", style = MaterialTheme.typography.titleLarge)
        RecentLogItem(log = DummyData.logHistory[0], series = DummyData.seriesList[2])
        RecentLogItem(log = DummyData.logHistory[1], series = DummyData.seriesList[0])
    }
}

@Composable
private fun RecentLogItem(log: LogEntry, series: Series) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(AccentOrange, shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            )
            Image(
                painter = painterResource(id = series.coverResId),
                contentDescription = series.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(12.dp)
                    .size(50.dp, 75.dp)
                    .clip(RoundedCornerShape(6.dp))
            )
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(series.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Chapter ${log.chapter} — 5 hours ago", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "\"${log.notes}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}