package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.dao.MonthlyCount
import com.example.codexiabeta.data.entity.SeriesWithGenres
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

enum class DateRange {
    THIS_WEEK,
    THIS_MONTH,
    THIS_YEAR,
    ALL_TIME
}

class InsightsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val logRepository = app.logRepository

    val selectedRange = MutableStateFlow(DateRange.ALL_TIME)

    private fun getStartTimestamp(range: DateRange): Long? {
        if (range == DateRange.ALL_TIME) return null
        val cal = Calendar.getInstance()
        when (range) {
            DateRange.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            DateRange.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            DateRange.THIS_YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
            }
            else -> {}
        }
        return cal.timeInMillis
    }

    val allSeries: StateFlow<List<SeriesWithGenres>> = seriesRepository.getAllSeriesWithGenres()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalLogCount: StateFlow<Int> = selectedRange.flatMapLatest { range ->
        logRepository.getTotalLogCount(getStartTimestamp(range), null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val monthlyLogCounts: StateFlow<List<MonthlyCount>> = selectedRange.flatMapLatest { range ->
        logRepository.getMonthlyLogCounts(getStartTimestamp(range), null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reading streak calculation
    val currentStreak: StateFlow<Int> = logRepository.getAllTimestamps()
        .map { timestamps -> calculateStreak(timestamps) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private fun calculateStreak(timestamps: List<Long>): Int {
        if (timestamps.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val uniqueDays = timestamps
            .map { dateFormat.format(Date(it)) }
            .distinct()
            .sortedDescending()

        val today = dateFormat.format(Date())
        if (uniqueDays.isEmpty() || uniqueDays[0] != today) return 0

        var streak = 1
        val cal = Calendar.getInstance()

        for (i in 1 until uniqueDays.size) {
            cal.time = dateFormat.parse(uniqueDays[i - 1])!!
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val expectedPreviousDay = dateFormat.format(cal.time)

            if (uniqueDays[i] == expectedPreviousDay) {
                streak++
            } else {
                break
            }
        }

        return streak
    }

    fun getStreakMessage(streak: Int): String {
        return when {
            streak == 0 -> "Start reading today to begin your streak!"
            streak >= 100 -> "🏆 LEGENDARY! $streak days in a row — you're a true arcane scholar!"
            streak >= 30 -> "🔥 $streak days in a row — a month of dedication! Incredible!"
            streak >= 7 -> "⭐ $streak days in a row — a full week! Keep the momentum!"
            else -> "You've read for $streak days in a row — keep it up!"
        }
    }

    fun getMonthLabel(yearMonth: String): String {
        return try {
            val parts = yearMonth.split("-")
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            monthNames[parts[1].toInt() - 1]
        } catch (e: Exception) {
            yearMonth
        }
    }

    fun onRangeSelected(range: DateRange) {
        selectedRange.value = range
    }
}
