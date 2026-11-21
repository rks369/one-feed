package org.rks369.news.routes

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respondRedirect
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.io.File

fun Route.webAppRoutes(webAppDir: File?) {
    get("/app") { call.respondRedirect("/app/", permanent = true) }

    if (webAppDir == null) {
        get("/app/{path...}") {
            call.respondText(
                "Web app not built. Run ./gradlew :app:webApp:wasmJsBrowserDistribution and restart the server.",
                status = HttpStatusCode.NotFound,
            )
        }
        return
    }

    staticFiles("/app", webAppDir) {
        default("index.html")
        contentType { file ->
            when (file.extension) {
                "wasm" -> ContentType("application", "wasm")
                "js", "mjs" -> ContentType.Text.JavaScript
                else -> null
            }
        }
    }
}
