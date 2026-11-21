package org.rks369.news.home.data

import kotlinx.serialization.Serializable

@Serializable
data class Article(
    val title: String,
    val abstract: String? = null,
    val url: String? = null,
    val section: String? = null,
    val byline: String? = null,
    val publishedDate: String? = null,
    val thumbnailUrl: String? = null,
)
