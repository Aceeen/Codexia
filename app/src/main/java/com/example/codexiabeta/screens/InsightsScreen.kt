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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.codexiabeta.model.DummyData

// Data class for our new chart. In a real app, this would be generated from a database query.
data class GenreCount(val genre: String, val count: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(navController: NavController) {
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
            MotivationalBanner()
            OverallSummaryCard()
            WeeklyActivityChart()
            GenrePieChart()
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// (MotivationalBanner, OverallSummaryCard, StatItem, and WeeklyActivityChart are unchanged)
@Composable
fun MotivationalBanner() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "You've logged reading for 12 days in a row—your dedication is inspiring!",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun OverallSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem("Total Chapters", "1,204")
            StatItem("Series Read", "4")
            StatItem("Top Genre", "Fantasy")
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
fun WeeklyActivityChart() {
    val dummyWeeklyData = listOf(0.4f, 0.6f, 0.2f, 0.9f, 0.5f, 0.7f, 0.3f) // Represents Sun - Sat
    Column {
        Text("Weekly Reading Activity", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dummyWeeklyData.forEach { value ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction = value)
                            .padding(horizontal = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}


@Composable
fun GenrePieChart() {
    val genreCounts = remember {

        val allGenres = DummyData.seriesList.flatMap { it.genres }

        allGenres.groupingBy { it }.eachCount()
            .map { (genre, count) -> GenreCount(genre, count) }
            .sortedByDescending { it.count }
    }

    val totalBooks = remember { DummyData.seriesList.size } // Total is still the number of series

    val pieChartColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
        MaterialTheme.colorScheme.surfaceVariant // Added more colors for more genres
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
                    // The denominator for the sweep angle is the total number of genre instances
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

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = DummyData.seriesList.size.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Titles",
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