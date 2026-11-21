package org.rks369.news.nyt

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.rks369.news.home.data.Article

class NytTopStoriesClient(private val apiKey: String) {
    private val httpClient = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun topStories(section: String): List<Article> {
        val response: NytResponse = httpClient.get("https://api.nytimes.com/svc/topstories/v2/$section.json") {
            parameter("api-key", apiKey)
        }.body()
        return response.results
            .filter { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() }
            .map {
                Article(
                    title = it.title!!,
                    abstract = it.abstract,
                    url = it.url,
                    section = it.section,
                    byline = it.byline,
                    publishedDate = it.publishedDate,
                    thumbnailUrl = it.multimedia?.firstOrNull()?.url,
                )
            }
    }
}

@Serializable
private data class NytResponse(val results: List<NytArticle> = emptyList())

@Serializable
private data class NytArticle(
    val title: String? = null,
    val abstract: String? = null,
    val url: String? = null,
    val section: String? = null,
    val byline: String? = null,
    @SerialName("published_date") val publishedDate: String? = null,
    val multimedia: List<NytMultimedia>? = null,
)

@Serializable
private data class NytMultimedia(val url: String? = null)
