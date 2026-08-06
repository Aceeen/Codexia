package com.example.codexiabeta.data.dao

import androidx.room.*
import com.example.codexiabeta.data.entity.SeriesEntity
import com.example.codexiabeta.data.entity.SeriesGenreCrossRef
import com.example.codexiabeta.data.entity.SeriesWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {

    @Transaction
    @Query("SELECT * FROM series ORDER BY lastUpdated DESC")
    fun getAllSeriesWithGenres(): Flow<List<SeriesWithGenres>>

    @Transaction
    @Query("SELECT * FROM series ORDER BY lastUpdated DESC")
    suspend fun getAllSeriesWithGenresOnce(): List<SeriesWithGenres>

    @Transaction
    @Query("SELECT * FROM series WHERE id = :seriesId")
    fun getSeriesWithGenresById(seriesId: String): Flow<SeriesWithGenres?>

    @Transaction
    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesWithGenresByIdOnce(seriesId: String): SeriesWithGenres?

    @Query("""
        SELECT * FROM series 
        WHERE (title LIKE '%' || :query || '%' OR author LIKE '%' || :query || '%')
        AND (:shelfId IS NULL OR shelfId = :shelfId)
        ORDER BY lastUpdated DESC
    """)
    fun searchSeries(query: String, shelfId: String?): Flow<List<SeriesEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: SeriesEntity)

    @Update
    suspend fun updateSeries(series: SeriesEntity)

    @Delete
    suspend fun deleteSeries(series: SeriesEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenreCrossRef(crossRef: SeriesGenreCrossRef)

    @Query("DELETE FROM series_genre_cross_ref WHERE seriesId = :seriesId")
    suspend fun deleteGenresForSeries(seriesId: String)

    @Query("UPDATE series SET latestChapter = :chapter WHERE id = :seriesId")
    suspend fun updateLatestChapter(seriesId: String, chapter: Int)
}
