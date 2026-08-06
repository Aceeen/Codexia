package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class SortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST,
    PINNED_FIRST,
    CHAPTER_ASC,
    CHAPTER_DESC
}

class SeriesDetailViewModel(
    application: Application,
    private val seriesId: String
) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val logRepository = app.logRepository

    val series: StateFlow<SeriesWithGenres?> = seriesRepository.getSeriesWithGenresById(seriesId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedSort = MutableStateFlow(SortOrder.NEWEST_FIRST)

    private val rawLogHistory: StateFlow<List<LogEntryEntity>> =
        logRepository.getLogEntriesBySeriesId(seriesId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logHistory: StateFlow<List<LogEntryEntity>> = combine(
        rawLogHistory,
        selectedSort
    ) { logs, sort ->
        when (sort) {
            SortOrder.NEWEST_FIRST -> logs.sortedByDescending { it.timestamp }
            SortOrder.OLDEST_FIRST -> logs.sortedBy { it.timestamp }
            SortOrder.PINNED_FIRST -> logs.sortedWith(
                compareByDescending<LogEntryEntity> { it.isPinned }
                    .thenByDescending { it.timestamp }
            )
            SortOrder.CHAPTER_ASC -> logs.sortedBy { it.chapter }
            SortOrder.CHAPTER_DESC -> logs.sortedByDescending { it.chapter }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestChapter: StateFlow<Int?> = logRepository.getLatestChapterForSeries(seriesId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun addLogEntry(chapter: Int, notes: String) {
        viewModelScope.launch {
            val existingLog = rawLogHistory.value.find { it.chapter == chapter }
            if (existingLog != null) {
                val updatedEntry = existingLog.copy(
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
                logRepository.updateLogEntry(updatedEntry)
            } else {
                val entry = LogEntryEntity(
                    id = UUID.randomUUID().toString(),
                    seriesId = seriesId,
                    chapter = chapter,
                    notes = notes,
                    timestamp = System.currentTimeMillis(),
                    isPinned = false
                )
                logRepository.insertLogEntry(entry)
                // Update latest chapter on the series if higher
                val currentLatest = series.value?.series?.latestChapter ?: 0
                if (chapter > currentLatest) {
                    seriesRepository.updateLatestChapter(seriesId, chapter)
                }
            }
        }
    }

    fun togglePin(entry: LogEntryEntity) {
        viewModelScope.launch {
            logRepository.updateLogEntry(entry.copy(isPinned = !entry.isPinned))
        }
    }

    fun deleteLogEntry(entry: LogEntryEntity) {
        viewModelScope.launch {
            logRepository.deleteLogEntry(entry)
        }
    }

    fun restoreLogEntry(entry: LogEntryEntity) {
        viewModelScope.launch {
            logRepository.insertLogEntry(entry)
        }
    }

    fun onSortChanged(sort: SortOrder) {
        selectedSort.value = sort
    }

    fun deleteSeries(onDeleted: () -> Unit) {
        viewModelScope.launch {
            series.value?.series?.let { s ->
                logRepository.deleteLogEntriesBySeriesId(seriesId)
                seriesRepository.deleteSeries(s)
                onDeleted()
            }
        }
    }
}
