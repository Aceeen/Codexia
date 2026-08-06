package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.entity.ShelfEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import kotlinx.coroutines.flow.*

class LibraryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val shelfRepository = app.shelfRepository

    val searchQuery = MutableStateFlow("")
    val selectedShelf = MutableStateFlow("All")

    val shelves: StateFlow<List<ShelfEntity>> = shelfRepository.getAllShelves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allSeries: StateFlow<List<SeriesWithGenres>> = seriesRepository.getAllSeriesWithGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSeries: StateFlow<List<SeriesWithGenres>> = combine(
        allSeries,
        searchQuery,
        selectedShelf,
        shelves
    ) { series, query, shelf, shelfList ->
        var filtered = series

        // Search filter
        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.series.title.contains(query, ignoreCase = true) ||
                it.series.author.contains(query, ignoreCase = true)
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

        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onShelfSelected(shelf: String) {
        selectedShelf.value = shelf
    }
}
