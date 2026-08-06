package com.example.codexiabeta.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.codexiabeta.R
import com.example.codexiabeta.data.dao.GenreDao
import com.example.codexiabeta.data.dao.LogEntryDao
import com.example.codexiabeta.data.dao.SeriesDao
import com.example.codexiabeta.data.dao.ShelfDao
import com.example.codexiabeta.data.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@Database(
    entities = [
        SeriesEntity::class,
        ShelfEntity::class,
        GenreEntity::class,
        SeriesGenreCrossRef::class,
        LogEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun seriesDao(): SeriesDao
    abstract fun shelfDao(): ShelfDao
    abstract fun logEntryDao(): LogEntryDao
    abstract fun genreDao(): GenreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "codexia_database"
                )
                    .addCallback(SeedDatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    seedDatabase(database)
                }
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            val shelfDao = database.shelfDao()
            val seriesDao = database.seriesDao()
            val genreDao = database.genreDao()
            val logEntryDao = database.logEntryDao()

            // Seed default shelves
            val webcomicsId = UUID.randomUUID().toString()
            val lightNovelsId = UUID.randomUUID().toString()
            val favoritesId = UUID.randomUUID().toString()

            shelfDao.insertShelf(ShelfEntity(id = webcomicsId, name = "Webcomics"))
            shelfDao.insertShelf(ShelfEntity(id = lightNovelsId, name = "Light Novels"))
            shelfDao.insertShelf(ShelfEntity(id = favoritesId, name = "Favorites"))

            // Seed genres
            val genres = listOf("Adventure", "Fantasy", "Action", "Apocalyptic", "Psychological", "Thriller")
            genres.forEach { genreDao.insertGenre(GenreEntity(it)) }

            // Seed series
            val series1 = SeriesEntity(
                id = "1",
                title = "Lord of The Mysteries",
                author = "Cuttlefish that Loves Diving",
                coverResId = R.drawable.cover_lord_of_the_mysteries,
                status = "Completed",
                latestChapter = 1325,
                totalChapters = 1432,
                lastUpdated = "1 year ago",
                shelfId = lightNovelsId,
                synopsis = "Who can come close to being a Beyonder?"
            )
            val series2 = SeriesEntity(
                id = "2",
                title = "Trash of The Count's Family",
                author = "Yu Ryeo Han",
                coverResId = R.drawable.cover_trash_of_the_counts_family,
                status = "Ongoing",
                latestChapter = 61,
                totalChapters = null,
                lastUpdated = "2 days ago",
                shelfId = webcomicsId,
                synopsis = "When I opened my eyes, I was inside a novel"
            )
            val series3 = SeriesEntity(
                id = "3",
                title = "Omniscient Reader's Viewpoint",
                author = "Sing-Shong",
                coverResId = R.drawable.cover_omniscient_readers_viewpoint,
                status = "Completed",
                latestChapter = 551,
                totalChapters = 551,
                lastUpdated = "6 months ago",
                shelfId = lightNovelsId,
                synopsis = "Kim Dokja's favorite webnovel suddenly becomes reality, and he is the only person who knows how the world will end. He must use his knowledge to survive the unfolding apocalypse."
            )
            val series4 = SeriesEntity(
                id = "4",
                title = "Kill the Sun",
                author = "Warmaisach",
                coverResId = R.drawable.cover_kill_the_sun,
                status = "On Hiatus",
                latestChapter = 550,
                totalChapters = null,
                lastUpdated = "1 month ago",
                shelfId = lightNovelsId,
                synopsis = "Earth, thousands of years after the apocalypse."
            )

            listOf(series1, series2, series3, series4).forEach { seriesDao.insertSeries(it) }

            // Seed genre cross-refs
            val crossRefs = listOf(
                SeriesGenreCrossRef("1", "Adventure"),
                SeriesGenreCrossRef("1", "Fantasy"),
                SeriesGenreCrossRef("2", "Adventure"),
                SeriesGenreCrossRef("2", "Fantasy"),
                SeriesGenreCrossRef("2", "Action"),
                SeriesGenreCrossRef("3", "Apocalyptic"),
                SeriesGenreCrossRef("3", "Psychological"),
                SeriesGenreCrossRef("4", "Adventure"),
                SeriesGenreCrossRef("4", "Fantasy"),
                SeriesGenreCrossRef("4", "Psychological"),
                SeriesGenreCrossRef("4", "Thriller")
            )
            crossRefs.forEach { seriesDao.insertGenreCrossRef(it) }

            // Seed log entries (using epoch millis)
            val logEntries = listOf(
                LogEntryEntity("l1", "3", 179, "Finished the final chapter! What an ending.", 1729468800000, false),
                LogEntryEntity("l2", "3", 150, "Praise the Fool!", 1728950400000, false),
                LogEntryEntity("l3", "3", 120, "Supporting character introduction is epic.", 1728086400000, false),
                LogEntryEntity("l4", "3", 100, "This broke me.", 1727481600000, false),
                // Additional entries for streak/insight testing
                LogEntryEntity("l5", "1", 1325, "Klein's journey is complete.", 1729382400000, false),
                LogEntryEntity("l6", "4", 550, "Waiting for the hiatus to end.", 1729296000000, false)
            )
            logEntries.forEach { logEntryDao.insertLogEntry(it) }
        }
    }
}
