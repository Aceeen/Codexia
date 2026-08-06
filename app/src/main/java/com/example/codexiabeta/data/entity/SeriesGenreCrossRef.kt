package com.example.codexiabeta.data.entity

import androidx.room.Entity

@Entity(
    tableName = "series_genre_cross_ref",
    primaryKeys = ["seriesId", "genreName"]
)
data class SeriesGenreCrossRef(
    val seriesId: String,
    val genreName: String
)
