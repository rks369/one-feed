package org.rks369.news.db

import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.ds.PGSimpleDataSource
import org.rks369.news.home.data.Article
import java.net.URI

class StoryStore(databaseUrl: String) {
    private val driver = dataSource(databaseUrl).asJdbcDriver()
    private val queries = NewsDatabase(driver).storiesQueries

    init {
        NewsDatabase.Schema.create(driver)
    }

    suspend fun fetchedAt(section: String): Long? = withContext(Dispatchers.IO) {
        queries.selectFetchedAt(section).executeAsOneOrNull()
    }

    suspend fun articles(section: String): List<Article> = withContext(Dispatchers.IO) {
        queries.selectArticles(section).executeAsList().map {
            Article(
                title = it.title,
                abstract = it.summary,
                url = it.url,
                section = it.article_section,
                byline = it.byline,
                publishedDate = it.published_date,
                thumbnailUrl = it.thumbnail_url,
            )
        }
    }

    suspend fun save(section: String, articles: List<Article>) = withContext(Dispatchers.IO) {
        queries.transaction {
            queries.deleteArticles(section)
            articles.distinctBy { it.url }.forEachIndexed { index, article ->
                queries.insertArticle(
                    section = section,
                    url = article.url!!,
                    position = index,
                    title = article.title,
                    summary = article.abstract,
                    byline = article.byline,
                    article_section = article.section,
                    published_date = article.publishedDate,
                    thumbnail_url = article.thumbnailUrl,
                )
            }
            queries.upsertFetchedAt(section, System.currentTimeMillis())
        }
    }
}

private fun dataSource(databaseUrl: String): PGSimpleDataSource {
    val uri = URI(databaseUrl.removePrefix("jdbc:"))
    val port = if (uri.port == -1) "" else ":${uri.port}"
    val query = uri.rawQuery?.let { "?$it" }.orEmpty()
    return PGSimpleDataSource().apply {
        setURL("jdbc:postgresql://${uri.host}$port${uri.rawPath}$query")
        uri.userInfo?.split(":", limit = 2)?.let { parts ->
            user = parts[0]
            password = parts.getOrNull(1)
        }
    }
}
