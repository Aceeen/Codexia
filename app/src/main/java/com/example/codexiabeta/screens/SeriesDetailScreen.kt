// In screens/SeriesDetailScreen.kt

package com.example.codexiabeta.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.codexiabeta.model.DummyData
import com.example.codexiabeta.model.LogEntry
import com.example.codexiabeta.model.Series
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(navController: NavController, seriesId: String) {
    val series = remember { DummyData.seriesList.first { it.id == seriesId } }

    // Fixed mutableStateOf syntax
    var logHistory by remember { mutableStateOf(DummyData.logHistory) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(series.title, maxLines = 1, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Updated: Functional Edit button
                    IconButton(onClick = { navController.navigate("addEditSeries") }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Series",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Masthead(series) }
            item { ProgressLogger() }
            item {
                Column {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Reading Log History",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            items(logHistory, key = { it.id }) { entry ->
                LogHistoryItem(
                    entry = entry,
                    onPinToggle = {
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        val updatedList = logHistory.map {
                            if (it.id == entry.id) it.copy(isPinned = !it.isPinned) else it
                        }
                        logHistory = updatedList.sortedWith(
                            compareByDescending<LogEntry> { it.isPinned }
                                .thenByDescending { it.timestamp }
                        )
                    },
                    onDelete = {
                        val deletedEntry = entry
                        val deletedEntryIndex = logHistory.indexOf(deletedEntry)
                        logHistory = logHistory.filterNot { it.id == deletedEntry.id }
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Entry deleted.",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                val mutableList = logHistory.toMutableList()
                                mutableList.add(deletedEntryIndex, deletedEntry)
                                logHistory = mutableList
                            }
                        }
                    }
                )
            }
        }
    }
}

// --- UPDATED MASTHEAD ---
@Composable
fun Masthead(series: Series) {
    Row(verticalAlignment = Alignment.Top) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Image(
                painter = painterResource(id = series.coverResId),
                contentDescription = "${series.title} cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(110.dp)
                    .height(165.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(series.title, style = MaterialTheme.typography.headlineMedium)
            Text(
                "by ${series.author}",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // NEW: Genre tags row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(series.genres) { genre ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                series.synopsis,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4 // Reduced from 5
            )
        }
    }
}

@Composable
fun ProgressLogger() {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Scribe a New Entry", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Chapter Number") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = "",
                onValueChange = {},
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { /* TODO: Implement log progress logic */ },
                modifier = Modifier.align(Alignment.End),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Progress")
            }
        }
    }
}

@Composable
fun LogHistoryItem(entry: LogEntry, onPinToggle: () -> Unit, onDelete: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (entry.isPinned)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300),
        label = "PinBackgroundColorAnimation"
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = if (entry.isPinned) 1.dp else 0.dp,
                color = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Chapter ${entry.chapter}", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onPinToggle) {
                    Icon(
                        imageVector = if (entry.isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Pin Highlight",
                        tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "\"${entry.notes}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                entry.timestamp,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
