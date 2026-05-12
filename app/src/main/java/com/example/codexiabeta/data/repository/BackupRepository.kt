package com.example.codexiabeta.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.room.withTransaction
import com.example.codexiabeta.data.AppDatabase
import com.example.codexiabeta.data.entity.LogEntryEntity
import com.example.codexiabeta.data.entity.SeriesEntity
import com.example.codexiabeta.data.entity.ShelfEntity
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

class BackupRepository(
    private val db: AppDatabase,
    private val seriesRepository: SeriesRepository
) {
    private val gson = Gson()

    suspend fun exportToCsv(uri: Uri, context: Context, userName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val seriesList = db.seriesDao().getAllSeriesWithGenresOnce()
            val allLogs = db.logEntryDao().getAllLogEntriesOnce()
            val allShelves = db.shelfDao().getAllShelvesOnce()

            val shelfMap = allShelves.associateBy { it.id }
            val logMap = allLogs.groupBy { it.seriesId }

            val rows = mutableListOf<List<String>>()
            // Header
            rows.add(listOf(
                "id", "title", "author", "synopsis", "status", "shelfName",
                "totalChapters", "sourceUrl", "genres", "logs", "coverUrl", "coverBase64",
                "latestChapter", "lastUpdated", "userName"
            ))

            for (swg in seriesList) {
                val series = swg.series
                val shelfName = shelfMap[series.shelfId]?.name ?: ""
                val genresStr = swg.genres.joinToString("|") { it.name }
                val logsStr = gson.toJson(logMap[series.id] ?: emptyList<LogEntryEntity>())

                var coverUrl = ""
                var coverBase64 = ""

                if (series.coverPath?.startsWith("http") == true) {
                    coverUrl = series.coverPath
                } else if (series.coverPath != null) {
                    val base64 = encodeImageToBase64(context, series.coverPath)
                    if (base64 != null) {
                        coverBase64 = base64
                    }
                }

                rows.add(listOf(
                    series.id,
                    series.title,
                    series.author,
                    series.synopsis,
                    series.status,
                    shelfName,
                    series.totalChapters?.toString() ?: "",
                    series.sourceUrl ?: "",
                    genresStr,
                    logsStr,
                    coverUrl,
                    coverBase64,
                    series.latestChapter.toString(),
                    series.lastUpdated,
                    userName
                ))
            }

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                csvWriter().writeAll(rows, outputStream)
            } ?: throw Exception("Could not open output stream")

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun importFromCsv(uri: Uri, context: Context, onUserNameImported: suspend (String) -> Unit): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: throw Exception("Could not open input stream")
            val rows: List<Map<String, String>> = csvReader().readAllWithHeader(inputStream)

            val logType = object : TypeToken<List<LogEntryEntity>>() {}.type
            
            var importedUserName: String? = null

            db.withTransaction {
                for (row in rows) {
                    if (importedUserName == null && row.containsKey("userName") && !row["userName"].isNullOrBlank()) {
                        importedUserName = row["userName"]
                    }

                    val id = row["id"] ?: UUID.randomUUID().toString()
                    val title = row["title"] ?: continue
                    val author = row["author"] ?: ""
                    val synopsis = row["synopsis"] ?: ""
                    val status = row["status"] ?: "Ongoing"
                    val shelfName = row["shelfName"] ?: ""
                    val totalChapters = row["totalChapters"]?.toIntOrNull()
                    val sourceUrl = row["sourceUrl"]?.takeIf { it.isNotBlank() }
                    val latestChapter = row["latestChapter"]?.toIntOrNull() ?: 0
                    val lastUpdated = row["lastUpdated"] ?: "Just now"

                    val genresStr = row["genres"] ?: ""
                    val genres = if (genresStr.isNotBlank()) genresStr.split("|") else emptyList()

                    val logsStr = row["logs"] ?: "[]"
                    val logs: List<LogEntryEntity> = try {
                        gson.fromJson(logsStr, logType)
                    } catch (e: Exception) { emptyList() }

                    val coverUrl = row["coverUrl"]
                    val coverBase64 = row["coverBase64"]

                    var finalCoverPath: String? = null
                    if (!coverUrl.isNullOrBlank()) {
                        finalCoverPath = coverUrl
                    } else if (!coverBase64.isNullOrBlank()) {
                        finalCoverPath = decodeBase64ToImage(context, coverBase64, id)
                    }

                    // Handle Shelf
                    var shelfId = ""
                    if (shelfName.isNotBlank()) {
                        var shelf = db.shelfDao().getShelfByName(shelfName)
                        if (shelf == null) {
                            shelf = ShelfEntity(name = shelfName)
                            db.shelfDao().insertShelf(shelf)
                        }
                        shelfId = shelf.id
                    }

                    val series = SeriesEntity(
                        id = id,
                        title = title,
                        author = author,
                        coverResId = com.example.codexiabeta.R.drawable.placeholder_cover,
                        coverPath = finalCoverPath,
                        status = status,
                        latestChapter = latestChapter,
                        totalChapters = totalChapters,
                        lastUpdated = lastUpdated,
                        shelfId = shelfId,
                        synopsis = synopsis,
                        sourceUrl = sourceUrl
                    )

                    // Overwrite series and genres
                    seriesRepository.insertSeries(series, genres)

                    // Insert logs
                    for (log in logs) {
                        db.logEntryDao().insertLogEntry(log.copy(seriesId = id)) // Ensure ID matches
                    }
                }
            }
            
            if (importedUserName != null) {
                onUserNameImported(importedUserName!!)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun encodeImageToBase64(context: Context, imagePath: String): String? {
        return try {
            val inputStream: InputStream? = if (imagePath.startsWith("content://")) {
                context.contentResolver.openInputStream(Uri.parse(imagePath))
            } else {
                File(imagePath).inputStream()
            }

            inputStream?.use {
                val bitmap = BitmapFactory.decodeStream(it) ?: return null
                // Compress to reduce CSV size (max 400x600 roughly)
                val ratio = 400f / bitmap.width
                val width = (bitmap.width * ratio).toInt()
                val height = (bitmap.height * ratio).toInt()
                val scaled = if (ratio < 1f) Bitmap.createScaledBitmap(bitmap, width, height, true) else bitmap

                val outputStream = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val bytes = outputStream.toByteArray()
                Base64.encodeToString(bytes, Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun decodeBase64ToImage(context: Context, base64Str: String, seriesId: String): String? {
        return try {
            val bytes = Base64.decode(base64Str, Base64.NO_WRAP)
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) coversDir.mkdirs()
            
            val file = File(coversDir, "cover_$seriesId.jpg")
            FileOutputStream(file).use { it.write(bytes) }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
