package com.example.codexiabeta.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.codexiabeta.CodexiaApplication
import com.example.codexiabeta.data.entity.SeriesEntity
import com.example.codexiabeta.data.entity.ShelfEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.UUID

class AddEditSeriesViewModel(
    application: Application,
    private val seriesId: String? = null
) : AndroidViewModel(application) {

    private val app = application as CodexiaApplication
    private val seriesRepository = app.seriesRepository
    private val shelfRepository = app.shelfRepository

    val isEditMode = seriesId != null

    // Form state
    var title by mutableStateOf("")
    var author by mutableStateOf("")
    var sourceUrl by mutableStateOf("")
    var synopsis by mutableStateOf("")
    var genres by mutableStateOf(listOf<String>())
    var selectedStatus by mutableStateOf("Ongoing")
    var selectedShelfName by mutableStateOf("")
    var selectedShelfId by mutableStateOf("")
    var totalChapters by mutableStateOf("")
    var coverPath by mutableStateOf<String?>(null)
    var coverResId by mutableStateOf(0)

    // Validation errors
    var titleError by mutableStateOf<String?>(null)
    var authorError by mutableStateOf<String?>(null)
    var sourceUrlError by mutableStateOf<String?>(null)
    var genreError by mutableStateOf<String?>(null)

    // Loading state
    var isLoading by mutableStateOf(true)
    var saveComplete by mutableStateOf(false)

    // Online Search State
    var onlineSearchResults by mutableStateOf<List<String>>(emptyList())
    var isSearchingOnline by mutableStateOf(false)
    var showOnlineSearchDialog by mutableStateOf(false)

    val shelves: StateFlow<List<ShelfEntity>> = shelfRepository.getAllShelves()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            if (seriesId != null) {
                val seriesWithGenres = seriesRepository.getSeriesWithGenresByIdOnce(seriesId)
                seriesWithGenres?.let { swg ->
                    title = swg.series.title
                    author = swg.series.author
                    sourceUrl = swg.series.sourceUrl ?: ""
                    synopsis = swg.series.synopsis
                    genres = swg.genres.map { it.name }
                    selectedStatus = swg.series.status
                    selectedShelfId = swg.series.shelfId
                    totalChapters = swg.series.totalChapters?.toString() ?: ""
                    coverPath = swg.series.coverPath
                    coverResId = swg.series.coverResId

                    // Resolve shelf name
                    shelves.first { it.isNotEmpty() }.let { shelfList ->
                        selectedShelfName = shelfList.find { it.id == swg.series.shelfId }?.name ?: ""
                    }
                }
            } else {
                // Default for new series - wait for shelves to load
                shelves.first { it.isNotEmpty() }.let { shelfList ->
                    selectedShelfName = shelfList.firstOrNull()?.name ?: ""
                    selectedShelfId = shelfList.firstOrNull()?.id ?: ""
                }
            }
            isLoading = false
        }
    }

    fun validate(): Boolean {
        var isValid = true

        titleError = if (title.isBlank()) {
            isValid = false
            "Title is required"
        } else null

        authorError = if (author.isBlank()) {
            isValid = false
            "Author is required"
        } else null

        sourceUrlError = if (sourceUrl.isNotBlank() && !sourceUrl.startsWith("http")) {
            isValid = false
            "URL must start with http:// or https://"
        } else null

        genreError = if (genres.isEmpty()) {
            isValid = false
            "At least one genre is required"
        } else null

        return isValid
    }

    fun save() {
        if (!validate()) return

        viewModelScope.launch {
            val series = SeriesEntity(
                id = seriesId ?: UUID.randomUUID().toString(),
                title = title.trim(),
                author = author.trim(),
                coverResId = if (coverResId != 0) coverResId else com.example.codexiabeta.R.drawable.placeholder_cover,
                coverPath = coverPath,
                status = selectedStatus,
                latestChapter = 0,
                totalChapters = totalChapters.toIntOrNull(),
                lastUpdated = "Just now",
                shelfId = selectedShelfId,
                synopsis = synopsis.trim(),
                sourceUrl = sourceUrl.trim().ifBlank { null }
            )

            if (isEditMode) {
                // Preserve latestChapter from existing
                val existing = seriesRepository.getSeriesWithGenresByIdOnce(seriesId!!)
                seriesRepository.updateSeries(
                    series.copy(latestChapter = existing?.series?.latestChapter ?: 0),
                    genres
                )
            } else {
                seriesRepository.insertSeries(series, genres)
            }
            saveComplete = true
        }
    }

    fun addGenre(genre: String) {
        if (genre.isNotBlank() && genre !in genres) {
            genres = genres + genre.trim()
            genreError = null
        }
    }

    fun removeGenre(genre: String) {
        genres = genres.filterNot { it == genre }
    }

    fun selectShelf(shelfName: String, shelfId: String) {
        selectedShelfName = shelfName
        selectedShelfId = shelfId
    }

    suspend fun createNewShelf(name: String): Result<ShelfEntity> {
        if (name.isBlank()) return Result.failure(Exception("Shelf name cannot be blank"))
        val existing = shelfRepository.getShelfByName(name)
        if (existing != null) return Result.failure(Exception("Shelf \"$name\" already exists"))

        val newShelf = ShelfEntity(name = name.trim())
        shelfRepository.insertShelf(newShelf)
        selectedShelfName = newShelf.name
        selectedShelfId = newShelf.id
        return Result.success(newShelf)
    }

    fun setCoverUri(uri: String?) {
        coverPath = uri
    }

    fun searchCoverOnline(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            isSearchingOnline = true
            val results = mutableListOf<String>()
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                
                // 1. Yahoo Image Search Scraping (Much more scraping-friendly than Google)
                try {
                    val yahooUrl = URL("https://images.search.yahoo.com/search/images?p=$encodedQuery")
                    val connection = yahooUrl.openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    connection.connectTimeout = 5000
                    if (connection.responseCode == 200) {
                        val html = connection.inputStream.bufferedReader().use { it.readText() }
                        // Yahoo uses Bing backend, images often look like https://tse1.mm.bing.net/...
                        // But let's just grab any valid image src that isn't a tiny icon
                        val pattern = java.util.regex.Pattern.compile("<img[^>]+src=[\"'](https?://[^\"]+?)[\"']")
                        val matcher = pattern.matcher(html)
                        while (matcher.find()) {
                            val url = matcher.group(1)
                            if (url != null && !url.contains("yimg.com") && !url.contains("yahoo.com")) {
                                results.add(url.replace("&amp;", "&"))
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                // 2. Google Books API (Fallback for higher quality / official covers)
                try {
                    val booksUrl = URL("https://www.googleapis.com/books/v1/volumes?q=$encodedQuery&maxResults=10")
                    val connection = booksUrl.openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val jsonObject = JSONObject(response)
                        val itemsArray = jsonObject.optJSONArray("items")
                        if (itemsArray != null) {
                            for (i in 0 until itemsArray.length()) {
                                val item = itemsArray.optJSONObject(i)
                                val volumeInfo = item?.optJSONObject("volumeInfo")
                                val thumbnail = volumeInfo?.optJSONObject("imageLinks")?.optString("thumbnail")
                                if (!thumbnail.isNullOrBlank()) {
                                    results.add(thumbnail.replace("http://", "https://"))
                                }
                            }
                        }
                    }
                } catch (e: Exception) { e.printStackTrace() }

                onlineSearchResults = results.distinct()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSearchingOnline = false
            }
        }
    }
}
