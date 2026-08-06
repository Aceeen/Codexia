package com.example.codexiabeta.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "shelves")
data class ShelfEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String
)
