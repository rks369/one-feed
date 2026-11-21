package org.rks369.news.home.data

import kotlinx.serialization.Serializable

@Serializable
data class Section(
    val key: String,
    val title: String,
)

object Sections {
    const val DEFAULT_KEY = "home"

    val all: List<Section> = listOf(
        Section(key = "arts", title = "Arts"),
        Section(key = "automobiles", title = "Automobiles"),
        Section(key = "books/review", title = "Books/Review"),
        Section(key = "business", title = "Business"),
        Section(key = "fashion", title = "Fashion"),
        Section(key = "food", title = "Food"),
        Section(key = "health", title = "Health"),
        Section(key = "home", title = "Home"),
        Section(key = "insider", title = "Insider"),
        Section(key = "magazine", title = "Magazine"),
        Section(key = "movies", title = "Movies"),
        Section(key = "nyregion", title = "New York Region"),
        Section(key = "obituaries", title = "Obituaries"),
        Section(key = "opinion", title = "Opinion"),
        Section(key = "politics", title = "Politics"),
        Section(key = "realestate", title = "Real Estate"),
        Section(key = "science", title = "Science"),
        Section(key = "sports", title = "Sports"),
        Section(key = "sundayreview", title = "Sunday Review"),
        Section(key = "technology", title = "Technology"),
        Section(key = "theater", title = "Theater"),
        Section(key = "t-magazine", title = "T Magazine"),
        Section(key = "travel", title = "Travel"),
        Section(key = "upshot", title = "The Upshot"),
        Section(key = "us", title = "United States"),
        Section(key = "world", title = "World"),
    )

    fun byKey(key: String): Section? = all.find { it.key == key }
}
