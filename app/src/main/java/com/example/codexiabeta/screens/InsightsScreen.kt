package com.example.codexiabeta.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.codexiabeta.viewmodel.DateRange
import com.example.codexiabeta.viewmodel.InsightsViewModel

// Data class for genre chart
data class GenreCount(val genre: String, val count: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    navController: NavController,
    viewModel: InsightsViewModel = viewModel()
) {
    val selectedRange by viewModel.selectedRange.collectAsStateWithLifecycle()
    val currentStreak by viewModel.currentStreak.collectAsStateWithLifecycle()
    val totalLogCount by viewModel.totalLogCount.collectAsStateWithLifecycle()
    val monthlyLogCounts by viewModel.monthlyLogCounts.collectAsStateWithLifecycle()
    val weeklyReadingActivity by viewModel.weeklyReadingActivity.collectAsStateWithLifecycle()
    val allSeries by viewModel.allSeries.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("The Oracle", style = MaterialTheme.typography.headlineSmall) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Date range filter chips
            DateRangeFilterRow(
                selectedRange = selectedRange,
                onRangeSelected = { viewModel.onRangeSelected(it) }
            )

            MotivationalBanner(message = viewModel.getStreakMessage(currentStreak))

            OverallSummaryCard(
                totalChapters = totalLogCount,
                seriesCount = allSeries.size,
                topGenre = allSeries
                    .flatMap { it.genres }
                    .groupingBy { it.name }
                    .eachCount()
                    .maxByOrNull { it.value }
                    ?.key ?: "—"
            )

            WeeklyActivityChart(weeklyReadingActivity)

            // Monthly activity chart - hide if selected range is THIS_WEEK or THIS_MONTH
            if (selectedRange != DateRange.THIS_WEEK && selectedRange != DateRange.THIS_MONTH && monthlyLogCounts.isNotEmpty()) {
                MonthlyActivityChart(
                    monthlyCounts = monthlyLogCounts.map {
                        Pair(viewModel.getMonthLabel(it.month), it.count)
                    }
                )
            }

            GenrePieChart(allSeries)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun DateRangeFilterRow(selectedRange: DateRange, onRangeSelected: (DateRange) -> Unit) {
    val options = listOf(
        "This Week" to DateRange.THIS_WEEK,
        "This Month" to DateRange.THIS_MONTH,
        "This Year" to DateRange.THIS_YEAR,
        "All Time" to DateRange.ALL_TIME
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        options.forEach { (label, range) ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MotivationalBanner(message: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            message,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OverallSummaryCard(totalChapters: Int, seriesCount: Int, topGenre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total Logs", totalChapters.toString())
            StatItem("Series Read", seriesCount.toString())
            StatItem("Top Genre", topGenre)
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklyActivityChart(weeklyData: List<Float>) {
    val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    Column {
        Text("Weekly Reading Activity", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weeklyData.forEach { value ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction = value.coerceAtLeast(0.02f))
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { label ->
                        Text(
                            label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyActivityChart(monthlyCounts: List<Pair<String, Int>>) {
    val maxCount = monthlyCounts.maxOfOrNull { it.second } ?: 1

    Column {
        Text("Monthly Reading Activity", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    monthlyCounts.reversed().forEach { (_, count) ->
                        val fraction = (count.toFloat() / maxCount).coerceIn(0.05f, 1f)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction = fraction)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        ) {
                            Text(
                                count.toString(),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    monthlyCounts.reversed().forEach { (label, _) ->
                        Text(
                            label,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenrePieChart(allSeries: List<com.example.codexiabeta.data.entity.SeriesWithGenres>) {
    val genreCounts = remember(allSeries) {
        val allGenres = allSeries.flatMap { swg -> swg.genres.map { it.name } }
        allGenres.groupingBy { it }.eachCount()
            .map { (genre, count) -> GenreCount(genre, count) }
            .sortedByDescending { it.count }
    }

    val pieChartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant
    )

    Column {
        Text("Genre Distribution (by Title)", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).height(150.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedPieChartCanvas(
                    modifier = Modifier.size(150.dp),
                    genreCounts = genreCounts,
                    colors = pieChartColors,
                    totalCount = genreCounts.sumOf { it.count }
                )
                Spacer(modifier = Modifier.width(24.dp))
                PieChartLegend(
                    genreCounts = genreCounts,
                    colors = pieChartColors
                )
            }
        }
    }
}

@Composable
fun AnimatedPieChartCanvas(
    modifier: Modifier = Modifier,
    genreCounts: List<GenreCount>,
    colors: List<Color>,
    totalCount: Int
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animateProgress = animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "PieChartAnimation"
    ).value

    LaunchedEffect(key1 = true) {
        animationPlayed = true
    }

    val seriesCount = genreCounts.sumOf { it.count }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (totalCount > 0) {
                var startAngle = -90f
                genreCounts.forEachIndexed { index, stat ->
                    val sweepAngle = (stat.count.toFloat() / totalCount) * 360f
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle * animateProgress,
                        useCenter = false,
                        style = Stroke(width = 35f, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = genreCounts.size.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Genres",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun PieChartLegend(genreCounts: List<GenreCount>, colors: List<Color>) {
    Column(verticalArrangement = Arrangement.Center) {
        genreCounts.forEachIndexed { index, stat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(colors[index % colors.size], shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${stat.genre} (${stat.count})",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 14.sp
                )
            }
        }
    }
}