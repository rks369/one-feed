package org.rks369.news.db

import app.cash.sqldelight.driver.jdbc.JdbcPreparedStatement
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
        val rows = articles.distinctBy { it.url }
        queries.transaction {
            queries.deleteArticles(section)
            if (rows.isNotEmpty()) insertArticles(section, rows)
            queries.upsertFetchedAt(section, System.currentTimeMillis())
        }
    }

    // One multi-row INSERT instead of one per article: each statement is a network round trip.
    private fun insertArticles(section: String, rows: List<Article>) {
        val placeholders = rows.joinToString(", ") { ROW_PLACEHOLDERS }
        driver.execute(
            null,
            "INSERT INTO article (section, url, position, title, summary, byline, article_section, published_date, thumbnail_url) VALUES $placeholders",
            rows.size * COLUMNS,
        ) {
            check(this is JdbcPreparedStatement)
            rows.forEachIndexed { index, article ->
                var parameter = index * COLUMNS
                bindString(parameter++, section)
                bindString(parameter++, article.url!!)
                bindInt(parameter++, index)
                bindString(parameter++, article.title)
                bindString(parameter++, article.abstract)
                bindString(parameter++, article.byline)
                bindString(parameter++, article.section)
                bindString(parameter++, article.publishedDate)
                bindString(parameter, article.thumbnailUrl)
            }
        }
    }
}

private const val COLUMNS = 9
private val ROW_PLACEHOLDERS = List(COLUMNS) { "?" }.joinToString(", ", "(", ")")

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
