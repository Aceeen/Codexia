package com.example.codexiabeta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey val id: String,
    val seriesId: String,
    val chapter: Int,
    val notes: String,
    val timestamp: Long, // epoch millis
    val isPinned: Boolean = false
)
