package com.example.codexiabeta.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import com.example.codexiabeta.ui.theme.AccentOrange
import com.example.codexiabeta.viewmodel.HomeViewModel
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    navController: NavController,
    onNavigateToLibrary: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()
    val recentLogs by viewModel.recentLogs.collectAsStateWithLifecycle()
    val greeting = viewModel.getGreeting()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        GreetingSection(
            navController = navController,
            greeting = greeting,
            userName = userName
        )
        ContinueReadingSection(
            navController = navController,
            seriesList = allSeries,
            onViewAllClick = onNavigateToLibrary
        )
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        RecentLogsSection(
            navController = navController,
            recentLogs = recentLogs,
            allSeries = allSeries
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GreetingSection(
    navController: NavController,
    greeting: String,
    userName: String
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$greeting, $userName.",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(4f, 4f),
                        blurRadius = 8f
                    )
                )
            )
            IconButton(onClick = { navController.navigate("profile") }) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Profile and Settings",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ContinueReadingSection(
    navController: NavController,
    seriesList: List<SeriesWithGenres>,
    onViewAllClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Continue Reading", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onViewAllClick) {
                Text("View All")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (seriesList.isEmpty()) {
            Text(
                "No series yet. Add one from the Library!",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            val pagerState = rememberPagerState(pageCount = { seriesList.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                pageSize = PageSize.Fixed(140.dp)
            ) { page ->
                val swg = seriesList[page]
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val scale = 1f + (0.05f * (1f - kotlin.math.abs(pageOffset).coerceIn(0f, 1f)))

                ContinueReadingCard(
                    series = swg,
                    scale = scale,
                    onClick = { navController.navigate("seriesDetail/${swg.series.id}") }
                )
            }
        }
    }
}

@Composable
private fun ContinueReadingCard(series: SeriesWithGenres, scale: Float, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            if (series.series.coverPath != null) {
                AsyncImage(
                    model = series.series.coverPath,
                    contentDescription = series.series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                )
            } else {
                Image(
                    painter = painterResource(id = series.series.coverResId),
                    contentDescription = series.series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(2f / 3f)
                )
            }
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
                    text = series.series.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Ch. ${series.series.latestChapter}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun RecentLogsSection(
    navController: NavController,
    recentLogs: List<LogEntryEntity>,
    allSeries: List<SeriesWithGenres>
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Recent Logs", style = MaterialTheme.typography.titleLarge)
        if (recentLogs.isEmpty()) {
            Text(
                "No logs yet. Start tracking your reading!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            recentLogs.take(3).forEach { log ->
                val series = allSeries.find { it.series.id == log.seriesId }
                if (series != null) {
                    RecentLogItem(log = log, series = series)
                }
            }
        }
    }
}

@Composable
private fun RecentLogItem(log: LogEntryEntity, series: SeriesWithGenres) {
    val timeAgo = getTimeAgo(log.timestamp)
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
            if (series.series.coverPath != null) {
                AsyncImage(
                    model = series.series.coverPath,
                    contentDescription = series.series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(50.dp, 75.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            } else {
                Image(
                    painter = painterResource(id = series.series.coverResId),
                    contentDescription = series.series.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(12.dp)
                        .size(50.dp, 75.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }
            Column(modifier = Modifier.padding(end = 12.dp)) {
                Text(series.series.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Chapter ${log.chapter} — $timeAgo", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (log.notes.isNotBlank()) {
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
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60000
    val hours = minutes / 60
    val days = hours / 24

    return when {
        minutes < 1 -> "Just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours hours ago"
        days < 7 -> "$days days ago"
        else -> {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            sdf.format(Date(timestamp))
        }
    }
}