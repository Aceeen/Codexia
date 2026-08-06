package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.SeriesWithGenres
import kotlinx.coroutines.flow.*
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val logRepository = app.logRepository
    private val userPreferences = app.userPreferences

    val userName: StateFlow<String> = userPreferences.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Reader")

    val allSeries: StateFlow<List<SeriesWithGenres>> = seriesRepository.getAllSeriesWithGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val allLogs: StateFlow<List<LogEntryEntity>> = logRepository.getAllLogEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueReadingSeries: StateFlow<List<SeriesWithGenres>> = combine(
        allSeries,
        allLogs
    ) { seriesList, logs ->
        val latestLogTimestampMap = logs
            .groupBy { it.seriesId }
            .mapValues { (_, seriesLogs) -> seriesLogs.maxOfOrNull { it.timestamp } ?: 0L }

        seriesList.sortedWith(
            compareByDescending<SeriesWithGenres> { swg ->
                latestLogTimestampMap[swg.series.id] ?: 0L
            }.thenByDescending { swg ->
                swg.series.lastUpdated
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<LogEntryEntity>> = logRepository.getRecentLogEntries(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            in 17..20 -> "Good Evening"
            else -> "Good Night"
        }
    }
}
