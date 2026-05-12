package com.example.codexiabeta

import android.app.Application
import com.example.codexiabeta.data.AppDatabase
import com.example.codexiabeta.data.UserPreferences
import com.example.codexiabeta.data.repository.LogRepository
import com.example.codexiabeta.data.repository.SeriesRepository
import com.example.codexiabeta.data.repository.ShelfRepository
import com.example.codexiabeta.data.repository.BackupRepository

class CodexiaApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }

    val seriesRepository: SeriesRepository by lazy {
        SeriesRepository(database.seriesDao(), database.genreDao())
    }

    val shelfRepository: ShelfRepository by lazy {
        ShelfRepository(database.shelfDao())
    }

    val logRepository: LogRepository by lazy {
        LogRepository(database.logEntryDao())
    }

    val userPreferences: UserPreferences by lazy {
        UserPreferences(this)
    }

    val backupRepository: BackupRepository by lazy {
        BackupRepository(database, seriesRepository)
    }
}
