package com.example.codexiabeta.data.dao

import androidx.room.*
import com.example.codexiabeta.data.entity.LogEntryEntity
import kotlinx.coroutines.flow.Flow

data class MonthlyCount(
    val month: String, // format: "YYYY-MM"
    val count: Int
)

@Dao
interface LogEntryDao {

    @Query("SELECT * FROM log_entries WHERE seriesId = :seriesId ORDER BY timestamp DESC")
    fun getLogEntriesBySeriesId(seriesId: String): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    fun getAllLogEntries(): Flow<List<LogEntryEntity>>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC")
    suspend fun getAllLogEntriesOnce(): List<LogEntryEntity>

    @Query("SELECT * FROM log_entries ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogEntries(limit: Int): Flow<List<LogEntryEntity>>

    @Query("""
        SELECT strftime('%Y-%m', timestamp / 1000, 'unixepoch') as month, 
               COUNT(*) as count 
        FROM log_entries 
        WHERE (:startTimestamp IS NULL OR timestamp >= :startTimestamp)
        AND (:endTimestamp IS NULL OR timestamp <= :endTimestamp)
        GROUP BY month 
        ORDER BY month DESC 
        LIMIT 6
    """)
    fun getMonthlyLogCounts(startTimestamp: Long?, endTimestamp: Long?): Flow<List<MonthlyCount>>

    @Query("""
        SELECT COUNT(*) FROM log_entries 
        WHERE (:startTimestamp IS NULL OR timestamp >= :startTimestamp)
        AND (:endTimestamp IS NULL OR timestamp <= :endTimestamp)
    """)
    fun getTotalLogCount(startTimestamp: Long?, endTimestamp: Long?): Flow<Int>

    @Query("""
        SELECT DISTINCT timestamp FROM log_entries 
        ORDER BY timestamp DESC
    """)
    fun getAllTimestamps(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogEntry(logEntry: LogEntryEntity)

    @Update
    suspend fun updateLogEntry(logEntry: LogEntryEntity)

    @Delete
    suspend fun deleteLogEntry(logEntry: LogEntryEntity)

    @Query("DELETE FROM log_entries WHERE id = :id")
    suspend fun deleteLogEntryById(id: String)

    @Query("DELETE FROM log_entries WHERE seriesId = :seriesId")
    suspend fun deleteLogEntriesBySeriesId(seriesId: String)

    @Query("SELECT MAX(chapter) FROM log_entries WHERE seriesId = :seriesId")
    fun getLatestChapterForSeries(seriesId: String): Flow<Int?>
}
