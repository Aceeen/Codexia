// In model/DummyData.kt

package com.example.codexiabeta.model

import androidx.annotation.DrawableRes
import com.example.codexiabeta.R

data class Series(
    val id: String,
    val title: String,
    val author: String,
    @DrawableRes val coverResId: Int, // Keep empty for now
    val status: String, // Ongoing, Completed, On Hiatus
    val latestChapter: Int,
    val lastUpdated: String,
    val shelf: String,
    val genres: List<String>,
    val synopsis: String // <-- ADDED THIS FIELD
)

data class LogEntry(
    val id: String,
    val chapter: Int,
    val notes: String,
    val timestamp: String,
    var isPinned: Boolean = false
)

object DummyData {
    val shelves = listOf("All", "Webcomics", "Light Novels", "Favorites")

    val seriesList = listOf(
        Series("1", "Lord of The Mysteries", "Cuttlefish that Loves Diving", R.drawable.cover_lord_of_the_mysteries, "Completed", 1325, "1 year ago", "Light Novels", listOf("Adventure", "Fantasy"), "Who can come close to being a Beyonder?"),
        Series("2", "Trash of The Count's Family", "Yu Ryeo Han", R.drawable.cover_trash_of_the_counts_family, "Ongoing", 61, "2 days ago", "Webcomics", listOf("Adventure", "Fantasy", "Action"), "When I opened my eyes, I was inside a novel"),
        Series("3", "Omniscient Reader's Viewpoint", "Sing-Shong", R.drawable.cover_omniscient_readers_viewpoint, "Completed", 551, "6 months ago", "Light Novels", listOf("Apocalyptic", "Psychological"), "Kim Dokja's favorite webnovel suddenly becomes reality, and he is the only person who knows how the world will end. He must use his knowledge to survive the unfolding apocalypse."),
        Series("4", "Kill the Sun", "Warmaisach", R.drawable.cover_kill_the_sun, "On Hiatus", 550, "1 month ago", "Light Novels", listOf("Adventure", "Fantasy", "Psychological", "Thriller"), "Earth, thousands of years after the apocalypse.")
    )

    val logHistory = listOf(
        LogEntry("l1", 179, "Finished the final chapter! What an ending.", "2024-10-21", false),
        LogEntry("l2", 150, "Praise the Fool!", "2024-10-15", false),
        LogEntry("l3", 120, "Supporting character introduction is epic.", "2024-10-05", false),
        LogEntry("l4", 100, "This broke me.", "2024-09-28", false)
    )
}