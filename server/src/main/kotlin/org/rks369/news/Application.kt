package org.rks369.news

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.cachingheaders.CachingHeaders
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.rks369.news.cache.TopStoriesCache
import org.rks369.news.config.MissingSettingException
import org.rks369.news.config.ServerConfig
import org.rks369.news.db.StoryStore
import org.rks369.news.nyt.NytTopStoriesClient
import org.rks369.news.routes.ApiError
import org.rks369.news.routes.apiRoutes
import org.rks369.news.routes.respondNotFoundPage
import org.rks369.news.routes.seoRoutes
import org.rks369.news.routes.webAppRoutes
import kotlin.system.exitProcess

fun main() {
    val config = try {
        ServerConfig.fromEnvironment()
    } catch (e: MissingSettingException) {
        System.err.println("Startup failed: ${e.message}")
        exitProcess(1)
    }
    embeddedServer(Netty, port = config.port, host = "0.0.0.0") {
        module(config)
    }.start(wait = true)
}

fun Application.module(config: ServerConfig) {
    val cache = TopStoriesCache(NytTopStoriesClient(config.nytApiKey), StoryStore(config.databaseUrl))

    install(ContentNegotiation) { json() }
    install(Compression)
    install(CachingHeaders)
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, _ ->
            if (call.request.path().startsWith("/api/")) {
                call.respond(HttpStatusCode.NotFound, ApiError("not_found", "No such endpoint."))
            } else {
                call.respondNotFoundPage(config)
            }
        }
    }

    routing {
        apiRoutes(cache)
        seoRoutes(cache, config)
        webAppRoutes(config.webAppDir)
    }
}
