package org.rks369.news.home.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.rks369.news.network.apiBaseUrl

class NewsRepository(
    private val httpClient: HttpClient = HttpClient {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    },
) {
    fun sections(): List<Section> = Sections.all

    suspend fun getArticles(sectionKey: String?): List<Article> {
        if (sectionKey == null) return emptyList()
        return httpClient.get("$apiBaseUrl/api/top-stories/$sectionKey").body()
    }
}
