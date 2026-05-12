package com.example.codexiabeta.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class SeriesWithGenres(
    @Embedded val series: SeriesEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "name",
        associateBy = Junction(
            value = SeriesGenreCrossRef::class,
            parentColumn = "seriesId",
            entityColumn = "genreName"
        )
    )
    val genres: List<GenreEntity>
)
