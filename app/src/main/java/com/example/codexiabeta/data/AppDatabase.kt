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
        }
    }
}
