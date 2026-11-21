package org.rks369.news.routes

import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.server.plugins.cachingheaders.caching
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.serialization.Serializable
import org.rks369.news.cache.NewsUnavailableException
import org.rks369.news.cache.TopStoriesCache
import org.rks369.news.cache.UnknownSectionException
import org.rks369.news.home.data.Sections

@Serializable
data class ApiError(val error: String, val message: String)

@Serializable
private data class Health(val status: String)

fun Route.apiRoutes(cache: TopStoriesCache) {
    get("/health") {
        call.respond(Health("ok"))
    }

    get("/api/sections") {
        call.caching = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 86_400, visibility = CacheControl.Visibility.Public))
        call.respond(Sections.all)
    }

    get("/api/top-stories/{section...}") {
        val section = call.parameters.getAll("section").orEmpty().joinToString("/")
        val articles = try {
            cache.get(section)
        } catch (e: UnknownSectionException) {
            return@get call.respond(HttpStatusCode.BadRequest, ApiError("unknown_section", "Unknown section '$section'."))
        } catch (e: NewsUnavailableException) {
            return@get call.respond(
                HttpStatusCode.BadGateway,
                ApiError("upstream_unavailable", "The news service is unavailable. Please try again later."),
            )
        }

        call.caching = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 300, visibility = CacheControl.Visibility.Public))
        call.respond(articles)
    }
}
