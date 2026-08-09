package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.entity.ShelfEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.GenreEntity
import kotlinx.coroutines.flow.*

enum class SortOption(val displayName: String) {
    LAST_UPDATED("Last Updated"),
    TITLE("Title")
}

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val shelfRepository = app.shelfRepository
    private val logRepository = app.logRepository

    val searchQuery = MutableStateFlow("")
    val selectedShelf = MutableStateFlow("All")
    val selectedSortOption = MutableStateFlow(SortOption.LAST_UPDATED)
    val selectedGenres = MutableStateFlow<Set<String>>(emptySet())

    val shelves: StateFlow<List<ShelfEntity>> = shelfRepository.getAllShelves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allGenres: StateFlow<List<String>> = seriesRepository.getAllGenres()
        .map { genres -> genres.map { it.name }.sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allSeries: StateFlow<List<SeriesWithGenres>> = seriesRepository.getAllSeriesWithGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allLogs: StateFlow<List<LogEntryEntity>> = logRepository.getAllLogEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSeries: StateFlow<List<SeriesWithGenres>> = combine(
        allSeries,
        searchQuery,
        selectedShelf,
        selectedSortOption,
        selectedGenres,
        allLogs,
        shelves
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        val series = array[0] as List<SeriesWithGenres>
        val query = array[1] as String
        val shelf = array[2] as String
        val sortOption = array[3] as SortOption
        @Suppress("UNCHECKED_CAST")
        val activeGenres = array[4] as Set<String>
        @Suppress("UNCHECKED_CAST")
        val logs = array[5] as List<LogEntryEntity>
        @Suppress("UNCHECKED_CAST")
        val shelfList = array[6] as List<ShelfEntity>

        var filtered = series

        // Search filter (matches title, author, or genre tags)
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.series.title.contains(query, ignoreCase = true) ||
                it.series.author.contains(query, ignoreCase = true) ||
                it.genres.any { it.name.contains(query, ignoreCase = true) }
            }
        }

        // Shelf filter
        if (shelf != "All") {
            val shelfEntity = shelfList.find { it.name == shelf }
            if (shelfEntity != null) {
                filtered = filtered.filter { swg ->
                    swg.shelves.any { it.id == shelfEntity.id }
                }
            }
        }

        // Genre filter (OR matching - contains any of the selected genres)
        if (activeGenres.isNotEmpty()) {
            filtered = filtered.filter { swg ->
                val bookGenres = swg.genres.map { it.name }
                activeGenres.any { it in bookGenres }
            }
        }

        // Sorting
        val latestLogTimestampMap = logs
            .groupBy { it.seriesId }
            .mapValues { (_, seriesLogs) -> seriesLogs.maxOfOrNull { it.timestamp } ?: 0L }

        val sorted = when (sortOption) {
            SortOption.LAST_UPDATED -> {
                filtered.sortedWith(
                    compareByDescending<SeriesWithGenres> { swg ->
                        latestLogTimestampMap[swg.series.id] ?: 0L
                    }.thenByDescending { swg ->
                        swg.series.lastUpdated
                    }
                )
            }
            SortOption.TITLE -> filtered.sortedBy { it.series.title.lowercase() }
        }

        sorted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onShelfSelected(shelf: String) {
        selectedShelf.value = shelf
    }

    fun onSortOptionSelected(option: SortOption) {
        selectedSortOption.value = option
    }

    fun toggleGenreFilter(genre: String) {
        selectedGenres.value = if (genre in selectedGenres.value) {
            selectedGenres.value - genre
        } else {
            selectedGenres.value + genre
        }
    }

    fun clearGenreFilter() {
        selectedGenres.value = emptySet()
    }
}
