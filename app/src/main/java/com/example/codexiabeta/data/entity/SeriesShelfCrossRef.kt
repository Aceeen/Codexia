package com.example.codexiabeta.data.entity

import androidx.room.Entity

@Entity(
    tableName = "series_shelf_cross_ref",
    primaryKeys = ["seriesId", "shelfId"]
)
data class SeriesShelfCrossRef(
    val seriesId: String,
    val shelfId: String
)
