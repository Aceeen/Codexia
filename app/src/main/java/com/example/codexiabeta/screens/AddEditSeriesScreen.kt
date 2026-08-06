package com.example.codexiabeta.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.codexiabeta.viewmodel.AddEditSeriesViewModel
import com.example.codexiabeta.viewmodel.AddEditSeriesViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSeriesScreen(navController: NavController, seriesId: String? = null) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val actualSeriesId = seriesId?.trim()?.ifBlank { null }

    val viewModel: AddEditSeriesViewModel = viewModel(
        factory = AddEditSeriesViewModelFactory(application, actualSeriesId)
    )

    val shelves by viewModel.shelves.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.setCoverUri(it.toString()) }
    }

    // Create new shelf dialog state
    var showCreateShelfDialog by remember { mutableStateOf(false) }
    var newShelfName by remember { mutableStateOf("") }
    var shelfDialogError by remember { mutableStateOf<String?>(null) }

    // Navigate back on save complete
    LaunchedEffect(viewModel.saveComplete) {
        if (viewModel.saveComplete) {
            navController.popBackStack()
        }
    }

    if (viewModel.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (viewModel.isEditMode) "Edit Series" else "Scribe's Desk",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = { viewModel.save() },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Add Cover Image", style = MaterialTheme.typography.titleMedium)

            // Cover preview
            if (viewModel.coverPath != null) {
                AsyncImage(
                    model = viewModel.coverPath,
                    contentDescription = "Selected cover",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Cover buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Gallery")
                }
                OutlinedButton(
                    onClick = {
                        viewModel.showOnlineSearchDialog = true
                        viewModel.searchCoverOnline("${viewModel.title} ${viewModel.author}".trim())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Search Online")
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Main Input Fields
            Text("Series Details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = {
                    viewModel.title = it
                    viewModel.titleError = null
                },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.titleError != null,
                supportingText = viewModel.titleError?.let { error -> { Text(error) } }
            )
            OutlinedTextField(
                value = viewModel.author,
                onValueChange = {
                    viewModel.author = it
                    viewModel.authorError = null
                },
                label = { Text("Author") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.authorError != null,
                supportingText = viewModel.authorError?.let { error -> { Text(error) } }
            )
            OutlinedTextField(
                value = viewModel.sourceUrl,
                onValueChange = {
                    viewModel.sourceUrl = it
                    viewModel.sourceUrlError = null
                },
                label = { Text("Source URL") },
                modifier = Modifier.fillMaxWidth(),
                isError = viewModel.sourceUrlError != null,
                supportingText = {
                    Text(viewModel.sourceUrlError ?: "Leave empty if not applicable.")
                }
            )
            OutlinedTextField(
                value = viewModel.totalChapters,
                onValueChange = { viewModel.totalChapters = it },
                label = { Text("Total Chapters (optional)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                supportingText = { Text("Leave empty for ongoing series.") }
            )
            OutlinedTextField(
                value = viewModel.synopsis,
                onValueChange = { viewModel.synopsis = it },
                label = { Text("Synopsis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- GENRE TAG INPUT SECTION ---
            Text("Genres", style = MaterialTheme.typography.titleMedium)
            if (viewModel.genreError != null) {
                Text(
                    viewModel.genreError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            val allGenresList by viewModel.allGenres.collectAsStateWithLifecycle()
            GenreTagInput(
                currentGenres = viewModel.genres,
                availableGenres = allGenresList.map { it.name },
                onGenreAdded = { viewModel.addGenre(it) },
                onGenreRemoved = { viewModel.removeGenre(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // Shelf and Status Assignment
            Text("Categorization", style = MaterialTheme.typography.titleMedium)
            StatusDropdown(
                selectedStatus = viewModel.selectedStatus,
                onStatusChanged = { viewModel.selectedStatus = it }
            )
            ShelfDropdown(
                selectedShelfIds = viewModel.selectedShelfIds,
                shelves = shelves,
                onShelfToggled = { id -> viewModel.toggleShelfSelection(id) },
                onCreateNewShelf = { showCreateShelfDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Create New Shelf Dialog
    if (showCreateShelfDialog) {
        AlertDialog(
            onDismissRequest = {
                showCreateShelfDialog = false
                newShelfName = ""
                shelfDialogError = null
            },
            title = { Text("Create New Shelf") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newShelfName,
                        onValueChange = {
                            newShelfName = it
                            shelfDialogError = null
                        },
                        label = { Text("Shelf Name") },
                        isError = shelfDialogError != null,
                        supportingText = shelfDialogError?.let { error -> { Text(error) } },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            val result = viewModel.createNewShelf(newShelfName)
                            result.fold(
                                onSuccess = {
                                    showCreateShelfDialog = false
                                    newShelfName = ""
                                    shelfDialogError = null
                                },
                                onFailure = {
                                    shelfDialogError = it.message
                                }
                            )
                        }
                    }
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCreateShelfDialog = false
                    newShelfName = ""
                    shelfDialogError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Online Image Search Dialog
    if (viewModel.showOnlineSearchDialog) {
        var searchQuery by remember { mutableStateOf("${viewModel.title} ${viewModel.author}".trim()) }

        AlertDialog(
            onDismissRequest = { viewModel.showOnlineSearchDialog = false },
            title = { Text("Search Cover Online") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("Search Query") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = { viewModel.searchCoverOnline(searchQuery) }
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    if (viewModel.isSearchingOnline) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (viewModel.onlineSearchResults.isEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            Text("No results found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 100.dp),
                            modifier = Modifier.weight(1f, fill = false).heightIn(max = 400.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.onlineSearchResults) { url ->
                                AsyncImage(
                                    model = url,
                                    contentDescription = "Cover result",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .aspectRatio(2f / 3f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            viewModel.setCoverUri(url)
                                            viewModel.showOnlineSearchDialog = false
                                        }
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    TextButton(
                        onClick = {
                            val query = Uri.encode("$searchQuery novel cover")
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?tbm=isch&q=$query"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Not finding it? Search on Google Images")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.showOnlineSearchDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreTagInput(
    currentGenres: List<String>,
    availableGenres: List<String>,
    onGenreAdded: (String) -> Unit,
    onGenreRemoved: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var isExpanded by remember { mutableStateOf(false) }

    val suggestions = remember(text, availableGenres, currentGenres) {
        if (text.isBlank()) {
            emptyList()
        } else {
            availableGenres.filter { 
                it.contains(text, ignoreCase = true) && it !in currentGenres 
            }
        }
    }

    LaunchedEffect(suggestions) {
        isExpanded = suggestions.isNotEmpty()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { }
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Add a genre tag") },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.isNotBlank()) {
                        onGenreAdded(text)
                        text = ""
                        isExpanded = false
                    }
                }),
                trailingIcon = {
                    IconButton(onClick = {
                        if (text.isNotBlank()) {
                            onGenreAdded(text)
                            text = ""
                            isExpanded = false
                        }
                    }) {
                        Icon(Icons.Default.AddCircle, contentDescription = "Add Genre")
                    }
                }
            )

            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    onDismissRequest = { isExpanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    suggestions.forEach { suggestion ->
                        DropdownMenuItem(
                            text = { Text(suggestion, color = MaterialTheme.colorScheme.onSurface) },
                            onClick = {
                                onGenreAdded(suggestion)
                                text = ""
                                isExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Horizontally scrolling row of existing genre chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(currentGenres) { genre ->
                InputChip(
                    selected = false,
                    onClick = { /* Not used */ },
                    label = { Text(genre) },
                    trailingIcon = {
                        IconButton(
                            onClick = { onGenreRemoved(genre) },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = "Remove $genre")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdown(selectedStatus: String, onStatusChanged: (String) -> Unit) {
    val items = listOf("Ongoing", "Completed", "On Hiatus", "Dropped")
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedStatus,
            onValueChange = {},
            readOnly = true,
            label = { Text("Status") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item, color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onStatusChanged(item)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShelfDropdown(
    selectedShelfIds: List<String>,
    shelves: List<com.example.codexiabeta.data.entity.ShelfEntity>,
    onShelfToggled: (String) -> Unit,
    onCreateNewShelf: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val displayText = remember(selectedShelfIds, shelves) {
        if (selectedShelfIds.isEmpty()) {
            "No shelves selected"
        } else {
            shelves.filter { it.id in selectedShelfIds }
                .joinToString(", ") { it.name }
        }
    }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Shelves") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            shelves.forEach { shelf ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = shelf.id in selectedShelfIds,
                                onCheckedChange = { onShelfToggled(shelf.id) },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary,
                                    uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(shelf.name, color = MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    onClick = {
                        onShelfToggled(shelf.id)
                    }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = {
                    Text(
                        "+ Create New Shelf",
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                onClick = {
                    isExpanded = false
                    onCreateNewShelf()
                }
            )
        }
    }
}
