// In screens/AddEditSeriesScreen.kt

package com.example.codexiabeta.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.codexiabeta.model.DummyData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSeriesScreen(navController: NavController) {
    // --- State holders for the form fields ---
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var sourceUrl by remember { mutableStateOf("") }
    var synopsis by remember { mutableStateOf("") }
    var genres by remember { mutableStateOf(listOf("Fantasy")) } // NEW: Genre list

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scribe's Desk", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            // TODO: Add save logic
                            navController.popBackStack()
                        },
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
                .verticalScroll(rememberScrollState()), // Makes the form scrollable
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text("Add Cover Image", style = MaterialTheme.typography.titleMedium)
            // Integrated Cover Search UI
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { /* TODO: Gallery logic */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("From Gallery")
                }
                OutlinedButton(onClick = { /* TODO: Search API logic */ }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Search Online")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Main Input Fields
            Text("Series Details", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = author, onValueChange = { author = it }, label = { Text("Author") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                label = { Text("Source URL") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("Leave empty if not applicable.") }
            )
            OutlinedTextField(
                value = synopsis,
                onValueChange = { synopsis = it },
                label = { Text("Synopsis") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- NEW: GENRE TAG INPUT SECTION ---
            Text("Genres", style = MaterialTheme.typography.titleMedium)
            GenreTagInput(
                currentGenres = genres,
                onGenreAdded = { newGenre ->
                    if (newGenre.isNotBlank() && newGenre !in genres)
                        genres = genres + newGenre
                },
                onGenreRemoved = { genreToRemove ->
                    genres = genres.filterNot { it == genreToRemove }
                }
            )
            // ---------------------------------

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // Shelf and Status Assignment
            Text("Categorization", style = MaterialTheme.typography.titleMedium)
            SimpleDropdownMenu(label = "Status", items = listOf("Ongoing", "Completed", "On Hiatus", "Dropped"))
            SimpleDropdownMenu(label = "Shelf", items = DummyData.shelves.filterNot { it == "All" } + "+ Create New Shelf")

            Spacer(modifier = Modifier.height(16.dp)) // Add space at the bottom
        }
    }
}

@Composable
fun GenreTagInput(
    currentGenres: List<String>,
    onGenreAdded: (String) -> Unit,
    onGenreRemoved: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Input field for new genres
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Add a genre tag") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                onGenreAdded(text)
                text = "" // Clear text after adding
            }),
            trailingIcon = {
                IconButton(onClick = {
                    onGenreAdded(text)
                    text = ""
                }) {
                    Icon(Icons.Default.AddCircle, contentDescription = "Add Genre")
                }
            }
        )

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
fun SimpleDropdownMenu(label: String, items: List<String>) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(items.firstOrNull() ?: "") }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = !isExpanded }
    ) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        if (item == "+ Create New Shelf") {
                            // TODO: Show a dialog to create a new shelf
                        } else {
                            selectedText = item
                        }
                        isExpanded = false
                    }
                )
            }
        }
    }
}
