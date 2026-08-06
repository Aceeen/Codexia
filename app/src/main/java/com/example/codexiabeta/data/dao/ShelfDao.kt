package com.example.codexiabeta.data.dao

import androidx.room.*
import com.example.codexiabeta.data.entity.ShelfEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {

    @Query("SELECT * FROM shelves ORDER BY name ASC")
    fun getAllShelves(): Flow<List<ShelfEntity>>

    @Query("SELECT * FROM shelves ORDER BY name ASC")
    suspend fun getAllShelvesOnce(): List<ShelfEntity>

    @Query("SELECT * FROM shelves WHERE name = :name LIMIT 1")
    suspend fun getShelfByName(name: String): ShelfEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertShelf(shelf: ShelfEntity)

    @Delete
    suspend fun deleteShelf(shelf: ShelfEntity)
}
