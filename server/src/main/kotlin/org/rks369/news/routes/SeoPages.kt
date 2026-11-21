package org.rks369.news.routes

import io.ktor.http.CacheControl
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.server.application.ApplicationCall
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondRedirect
import io.ktor.server.plugins.cachingheaders.caching
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.a
import kotlinx.html.article
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.footer
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.header
import kotlinx.html.img
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.main
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.span
import kotlinx.html.time
import kotlinx.html.title
import org.rks369.news.cache.TopStoriesCache
import org.rks369.news.config.ServerConfig
import org.rks369.news.home.data.Article
import org.rks369.news.home.data.Section
import org.rks369.news.home.data.Sections
import org.rks369.news.util.formatPublishedDate

private const val SITE_NAME = "One Feed"

fun Route.seoRoutes(cache: TopStoriesCache, config: ServerConfig) {
    staticResources("/static", "static") {
        cacheControl { listOf(CacheControl.MaxAge(maxAgeSeconds = 3600, visibility = CacheControl.Visibility.Public)) }
    }

    get("/favicon.ico") { call.respondRedirect("/static/favicon.ico", permanent = true) }

    get("/") {
        val home = Sections.byKey(Sections.DEFAULT_KEY)!!
        call.respondSectionPage(cache, config, home, path = "/", isHome = true)
    }

    get("/section/{key...}") {
        val key = call.parameters.getAll("key").orEmpty().joinToString("/")
        val section = Sections.byKey(key) ?: return@get call.respondNotFoundPage(config)
        if (section.key == Sections.DEFAULT_KEY) return@get call.respondRedirect("/", permanent = true)
        call.respondSectionPage(cache, config, section, path = sectionPath(section), isHome = false)
    }
}

private suspend fun ApplicationCall.respondSectionPage(
    cache: TopStoriesCache,
    config: ServerConfig,
    section: Section,
    path: String,
    isHome: Boolean,
) {
    val articles = runCatching { cache.get(section.key) }.getOrNull()
    val heading = if (isHome) "Top Stories" else "${section.title} · Top Stories"
    val description = if (isHome) {
        "Today's top stories from The New York Times, rendered on a Kotlin Ktor server."
    } else {
        "The latest ${section.title} top stories from The New York Times."
    }

    caching = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 300, visibility = CacheControl.Visibility.Public))
    respondHtml(if (articles == null) HttpStatusCode.ServiceUnavailable else HttpStatusCode.OK) {
        page(
            config = config,
            title = "$heading | $SITE_NAME",
            description = description,
            path = path,
            currentSection = section,
            ogImage = articles?.firstNotNullOfOrNull { it.thumbnailUrl },
        ) {
            h1 { +heading }
            if (articles == null) {
                p(classes = "notice") { +"Stories are temporarily unavailable. Please try again in a minute." }
            } else {
                articles.forEach { articleCard(it) }
            }
        }
    }
}

suspend fun ApplicationCall.respondNotFoundPage(config: ServerConfig) {
    respondHtml(HttpStatusCode.NotFound) {
        page(
            config = config,
            title = "Page not found | $SITE_NAME",
            description = "This page does not exist.",
            path = null,
            currentSection = null,
            ogImage = null,
        ) {
            h1 { +"Page not found" }
            p { +"We couldn't find that section. Pick one above or head back to the "; a(href = "/") { +"top stories" }; +"." }
        }
    }
}

private fun HTML.page(
    config: ServerConfig,
    title: String,
    description: String,
    path: String?,
    currentSection: Section?,
    ogImage: String?,
    content: FlowContent.() -> Unit,
) {
    lang = "en"
    head {
        meta(charset = "utf-8")
        meta(name = "viewport", content = "width=device-width, initial-scale=1")
        title { +title }
        meta(name = "description", content = description)
        if (path != null) {
            link(rel = "canonical", href = config.publicBaseUrl + path)
            meta { attributes["property"] = "og:url"; this.content = config.publicBaseUrl + path }
        } else {
            meta(name = "robots", content = "noindex")
        }
        meta { attributes["property"] = "og:type"; this.content = "website" }
        meta { attributes["property"] = "og:site_name"; this.content = SITE_NAME }
        meta { attributes["property"] = "og:title"; this.content = title }
        meta { attributes["property"] = "og:description"; this.content = description }
        meta { attributes["property"] = "og:image"; this.content = ogImage ?: (config.publicBaseUrl + "/static/og-default.png") }
        meta(name = "twitter:card", content = "summary_large_image")
        meta(name = "color-scheme", content = "light dark")
        link(rel = "icon", href = "/static/favicon.svg", type = "image/svg+xml")
        link(rel = "icon", href = "/static/favicon.ico") { attributes["sizes"] = "32x32" }
        link(rel = "apple-touch-icon", href = "/static/apple-touch-icon.png")
        link(rel = "stylesheet", href = "/static/site.css")
        script(src = "/static/site.js") { defer = true }
    }
    body {
        header(classes = "site-header") {
            div(classes = "bar") {
                a(href = "/", classes = "brand") {
                    img(src = "/static/favicon.svg", alt = "") {
                        attributes["width"] = "28"
                        attributes["height"] = "28"
                    }
                    +SITE_NAME
                }
                a(href = "/app/", classes = "cta") { +"Open the app →" }
            }
            sectionTabs(currentSection)
        }
        main { content() }
        footer {
            p {
                +"Data provided by "
                a(href = "https://developer.nytimes.com") { +"The New York Times" }
                +"."
            }
        }
    }
}

private fun FlowContent.articleCard(article: Article) {
    article(classes = "story") {
        article.thumbnailUrl?.let { src ->
            if (article.url != null) {
                a(href = article.url, target = "_blank", classes = "thumb") {
                    attributes["rel"] = "noopener"
                    attributes["tabindex"] = "-1"
                    attributes["aria-hidden"] = "true"
                    storyImage(src, article.title)
                }
            } else {
                storyImage(src, article.title)
            }
        }
        h2 {
            if (article.url != null) {
                a(href = article.url, target = "_blank") {
                    attributes["rel"] = "noopener"
                    +article.title
                }
            } else {
                +article.title
            }
        }
        article.abstract?.let { p { +it } }
        p(classes = "meta") {
            article.byline?.let { span { +it } }
            formatPublishedDate(article.publishedDate)?.let { formatted ->
                time { attributes["datetime"] = article.publishedDate.orEmpty(); +formatted }
            }
        }
    }
}

private fun FlowContent.storyImage(src: String, title: String) {
    img(src = src, alt = title) {
        attributes["loading"] = "lazy"
        attributes["width"] = "440"
        attributes["height"] = "293"
    }
}

private fun FlowContent.sectionTabs(current: Section?) {
    val home = Sections.byKey(Sections.DEFAULT_KEY)!!
    nav(classes = "tabs") {
        attributes["aria-label"] = "Sections"
        (listOf(home) + (Sections.all - home)).forEach { section ->
            a(href = sectionPath(section)) {
                if (section == current) attributes["aria-current"] = "page"
                +section.title
            }
        }
    }
}

private fun sectionPath(section: Section) =
    if (section.key == Sections.DEFAULT_KEY) "/" else "/section/${section.key}"
