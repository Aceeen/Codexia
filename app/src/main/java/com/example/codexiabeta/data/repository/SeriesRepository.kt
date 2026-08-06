package com.example.codexiabeta.data.repository

import com.example.codexiabeta.data.dao.GenreDao
import com.example.codexiabeta.data.dao.SeriesDao
import com.example.codexiabeta.data.entity.*
import kotlinx.coroutines.flow.Flow

class SeriesRepository(
    private val seriesDao: SeriesDao,
    private val genreDao: GenreDao
) {
    fun getAllSeriesWithGenres(): Flow<List<SeriesWithGenres>> =
        seriesDao.getAllSeriesWithGenres()

    fun getSeriesWithGenresById(seriesId: String): Flow<SeriesWithGenres?> =
        seriesDao.getSeriesWithGenresById(seriesId)

    fun getAllGenres(): Flow<List<GenreEntity>> =
        genreDao.getAllGenres()

    suspend fun getSeriesWithGenresByIdOnce(seriesId: String): SeriesWithGenres? =
        seriesDao.getSeriesWithGenresByIdOnce(seriesId)

    suspend fun insertSeries(series: SeriesEntity, genres: List<String>, shelfIds: List<String>) {
        seriesDao.insertSeries(series)
        // Insert genres and cross-refs
        genres.forEach { genreName ->
            genreDao.insertGenre(GenreEntity(genreName))
            seriesDao.insertGenreCrossRef(SeriesGenreCrossRef(series.id, genreName))
        }
        // Insert shelf cross-refs
        shelfIds.forEach { shelfId ->
            seriesDao.insertShelfCrossRef(SeriesShelfCrossRef(series.id, shelfId))
        }
    }

    suspend fun updateSeries(series: SeriesEntity, genres: List<String>, shelfIds: List<String>) {
        seriesDao.updateSeries(series)
        // Re-create genre cross-refs
        seriesDao.deleteGenresForSeries(series.id)
        genres.forEach { genreName ->
            genreDao.insertGenre(GenreEntity(genreName))
            seriesDao.insertGenreCrossRef(SeriesGenreCrossRef(series.id, genreName))
        }
        // Re-create shelf cross-refs
        seriesDao.deleteShelvesForSeries(series.id)
        shelfIds.forEach { shelfId ->
            seriesDao.insertShelfCrossRef(SeriesShelfCrossRef(series.id, shelfId))
        }
    }

    suspend fun deleteSeries(series: SeriesEntity) {
        seriesDao.deleteGenresForSeries(series.id)
        seriesDao.deleteShelvesForSeries(series.id)
        seriesDao.deleteSeries(series)
    }

    suspend fun updateLatestChapter(seriesId: String, chapter: Int) {
        seriesDao.updateLatestChapter(seriesId, chapter)
    }
}
