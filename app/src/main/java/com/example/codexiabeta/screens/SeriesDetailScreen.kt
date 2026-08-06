package com.example.codexiabeta.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import com.example.codexiabeta.viewmodel.SeriesDetailViewModel
import com.example.codexiabeta.viewmodel.SeriesDetailViewModelFactory
import com.example.codexiabeta.viewmodel.SortOrder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(navController: NavController, seriesId: String) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: SeriesDetailViewModel = viewModel(
        factory = SeriesDetailViewModelFactory(application, seriesId)
    )

    val series by viewModel.series.collectAsStateWithLifecycle()
    val logHistory by viewModel.logHistory.collectAsStateWithLifecycle()
    val selectedSort by viewModel.selectedSort.collectAsStateWithLifecycle()
    val latestChapter by viewModel.latestChapter.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    var editingLogEntry by remember { mutableStateOf<LogEntryEntity?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var pendingLogData by remember { mutableStateOf<Pair<Int, String>?>(null) }

    val currentSeries = series ?: return

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(currentSeries.series.title, maxLines = 1, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Series",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    // Edit button now navigates with series ID
                    IconButton(onClick = { navController.navigate("addEditSeries/${currentSeries.series.id}") }) {
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
            item {
                Masthead(
                    series = currentSeries,
                    latestChapter = latestChapter
                )
            }
            item {
                ProgressLogger(
                    editingEntry = editingLogEntry,
                    onLogEntry = { chapter, notes ->
                        if (editingLogEntry != null) {
                            viewModel.addLogEntry(chapter, notes)
                            editingLogEntry = null
                            scope.launch {
                                snackbarHostState.showSnackbar("Entry updated.")
                            }
                        } else {
                            val chapterExists = logHistory.any { it.chapter == chapter }
                            if (chapterExists) {
                                pendingLogData = Pair(chapter, notes)
                            } else {
                                viewModel.addLogEntry(chapter, notes)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Entry logged for Chapter $chapter.")
                                }
                            }
                        }
                    },
                    onCancelEdit = {
                        editingLogEntry = null
                    }
                )
            }
            item {
                Column {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        "Reading Log History",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
            // Sort chips
            item {
                SortChipRow(
                    selectedSort = selectedSort,
                    onSortChanged = { viewModel.onSortChanged(it) }
                )
            }
            items(logHistory, key = { it.id }) { entry ->
                SwipeToDismissLogItem(
                    entry = entry,
                    onPinToggle = {
                        haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                        viewModel.togglePin(entry)
                    },
                    onEdit = {
                        editingLogEntry = entry
                    },
                    onDelete = {
                        viewModel.deleteLogEntry(entry)
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Entry deleted.",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreLogEntry(entry)
                            }
                        }
                    }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Series") },
            text = { Text("Are you sure you want to delete '${currentSeries.series.title}'? This action cannot be undone and will delete all associated logs.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteSeries {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    pendingLogData?.let { (chapter, notes) ->
        AlertDialog(
            onDismissRequest = { pendingLogData = null },
            title = { Text("Overwrite Reading Log?") },
            text = { Text("A log for Chapter $chapter already exists. Saving this will overwrite the previous notes and timestamp.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.addLogEntry(chapter, notes)
                        pendingLogData = null
                        scope.launch {
                            snackbarHostState.showSnackbar("Chapter $chapter log overwritten.")
                        }
                    }
                ) {
                    Text("Overwrite")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingLogData = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SortChipRow(selectedSort: SortOrder, onSortChanged: (SortOrder) -> Unit) {
    val options = listOf(
        "Newest" to SortOrder.NEWEST_FIRST,
        "Oldest" to SortOrder.OLDEST_FIRST,
        "Pinned" to SortOrder.PINNED_FIRST,
        "Ch. ↑" to SortOrder.CHAPTER_ASC,
        "Ch. ↓" to SortOrder.CHAPTER_DESC
    )
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (label, sort) ->
            FilterChip(
                selected = selectedSort == sort,
                onClick = { onSortChanged(sort) },
                label = { Text(label, style = MaterialTheme.typography.bodySmall) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDismissLogItem(
    entry: LogEntryEntity,
    onPinToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE53935))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = {
            LogHistoryItem(
                entry = entry,
                onPinToggle = onPinToggle,
                onEdit = onEdit,
                onDelete = onDelete
            )
        }
    )
}

// --- UPDATED MASTHEAD ---
@Composable
fun Masthead(series: SeriesWithGenres, latestChapter: Int?) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.Top) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (series.series.coverPath != null) {
                AsyncImage(
                    model = series.series.coverPath,
                    contentDescription = "${series.series.title} cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                )
            } else {
                Image(
                    painter = painterResource(id = series.series.coverResId),
                    contentDescription = "${series.series.title} cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(110.dp)
                        .height(165.dp)
                )
            }
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.animateContentSize()) {
            Text(series.series.title, style = MaterialTheme.typography.headlineMedium)
            Text(
                "by ${series.series.author}",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            // Genre tags row
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(series.genres) { genre ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text(
                            text = genre.name,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Chapter progress bar
            val currentCh = latestChapter ?: series.series.latestChapter
            val totalCh = series.series.totalChapters
            if (totalCh != null && totalCh > 0) {
                val progress = (currentCh.toFloat() / totalCh).coerceIn(0f, 1f)
                Column {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ch. $currentCh / $totalCh",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Text(
                        "Ongoing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Source URL button
            if (!series.series.sourceUrl.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(series.series.sourceUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Open Source", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Expandable synopsis — only shown if there is content
            if (series.series.synopsis.isNotBlank()) {
                Text(
                    series.series.synopsis,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (isExpanded) Int.MAX_VALUE else 4
                )
                TextButton(
                    onClick = { isExpanded = !isExpanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        if (isExpanded) "Show less" else "Show more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun ProgressLogger(
    editingEntry: LogEntryEntity?,
    onLogEntry: (Int, String) -> Unit,
    onCancelEdit: () -> Unit
) {
    var chapterInput by remember { mutableStateOf("") }
    var notesInput by remember { mutableStateOf("") }
    var chapterError by remember { mutableStateOf<String?>(null) }

    // Pre-fill when editing
    LaunchedEffect(editingEntry) {
        if (editingEntry != null) {
            chapterInput = editingEntry.chapter.toString()
            notesInput = editingEntry.notes
            chapterError = null
        } else {
            chapterInput = ""
            notesInput = ""
            chapterError = null
        }
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(if (editingEntry != null) "Edit Entry" else "Scribe a New Entry", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = chapterInput,
                onValueChange = {
                    chapterInput = it
                    chapterError = null
                },
                label = { Text("Chapter Number") },
                modifier = Modifier.fillMaxWidth(),
                isError = chapterError != null,
                supportingText = chapterError?.let { error -> { Text(error) } },
                readOnly = editingEntry != null // Can't change chapter number when editing
            )
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (editingEntry != null) {
                    TextButton(onClick = onCancelEdit) {
                        Text("Cancel")
                    }
                }
                Button(
                    onClick = {
                        val chapter = chapterInput.toIntOrNull()
                        if (chapter == null || chapter <= 0) {
                            chapterError = "Please enter a valid positive number"
                            return@Button
                        }
                        onLogEntry(chapter, notesInput)
                        chapterInput = ""
                        notesInput = ""
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(if (editingEntry != null) Icons.Default.Save else Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (editingEntry != null) "Save" else "Log Progress")
                }
            }
        }
    }
}

@Composable
fun LogHistoryItem(entry: LogEntryEntity, onPinToggle: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (entry.isPinned)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 300),
        label = "PinBackgroundColorAnimation"
    )

    val dateStr = remember(entry.timestamp) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(entry.timestamp))
    }

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
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Note",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onPinToggle) {
                        Icon(
                            imageVector = if (entry.isPinned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Pin Highlight",
                            tint = if (entry.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "\"${entry.notes}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                dateStr,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}
