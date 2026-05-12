package com.example.codexiabeta.data.repository

import com.example.codexiabeta.data.dao.LogEntryDao
import com.example.codexiabeta.data.dao.MonthlyCount
import com.example.codexiabeta.data.entity.LogEntryEntity
import kotlinx.coroutines.flow.Flow

class LogRepository(private val logEntryDao: LogEntryDao) {

    fun getLogEntriesBySeriesId(seriesId: String): Flow<List<LogEntryEntity>> =
        logEntryDao.getLogEntriesBySeriesId(seriesId)

    fun getAllLogEntries(): Flow<List<LogEntryEntity>> =
        logEntryDao.getAllLogEntries()

    fun getRecentLogEntries(limit: Int = 5): Flow<List<LogEntryEntity>> =
        logEntryDao.getRecentLogEntries(limit)

    fun getMonthlyLogCounts(startTimestamp: Long?, endTimestamp: Long?): Flow<List<MonthlyCount>> =
        logEntryDao.getMonthlyLogCounts(startTimestamp, endTimestamp)

    fun getTotalLogCount(startTimestamp: Long?, endTimestamp: Long?): Flow<Int> =
        logEntryDao.getTotalLogCount(startTimestamp, endTimestamp)

    fun getAllTimestamps(): Flow<List<Long>> =
        logEntryDao.getAllTimestamps()

    fun getLatestChapterForSeries(seriesId: String): Flow<Int?> =
        logEntryDao.getLatestChapterForSeries(seriesId)

    suspend fun insertLogEntry(logEntry: LogEntryEntity) =
        logEntryDao.insertLogEntry(logEntry)

    suspend fun updateLogEntry(logEntry: LogEntryEntity) =
        logEntryDao.updateLogEntry(logEntry)

    suspend fun deleteLogEntry(logEntry: LogEntryEntity) =
        logEntryDao.deleteLogEntry(logEntry)

    suspend fun deleteLogEntryById(id: String) =
        logEntryDao.deleteLogEntryById(id)
}
