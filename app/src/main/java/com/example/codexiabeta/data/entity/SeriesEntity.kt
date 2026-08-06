package com.example.codexiabeta.data.entity

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "series")
data class SeriesEntity(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    @DrawableRes val coverResId: Int,
    val coverPath: String? = null,
    val status: String,
    val latestChapter: Int,
    val totalChapters: Int? = null,
    val lastUpdated: String,
    val shelfId: String,
    val synopsis: String,
    val sourceUrl: String? = null
)
