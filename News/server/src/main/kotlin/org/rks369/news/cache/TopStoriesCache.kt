package org.rks369.news.cache

import org.rks369.news.db.StoryStore
import org.rks369.news.home.data.Article
import org.rks369.news.home.data.Sections
import org.rks369.news.nyt.NytTopStoriesClient
import org.slf4j.LoggerFactory

private const val TEN_MINUTES = 10 * 60 * 1000L

class TopStoriesCache(
    private val nyt: NytTopStoriesClient,
    private val store: StoryStore,
) {
    private val log = LoggerFactory.getLogger(TopStoriesCache::class.java)

    suspend fun get(section: String): List<Article> {
        if (Sections.byKey(section) == null) throw UnknownSectionException(section)

        val fetchedAt = store.fetchedAt(section)
        if (fetchedAt != null && System.currentTimeMillis() - fetchedAt < TEN_MINUTES) {
            return store.articles(section)
        }

        return try {
            val articles = nyt.topStories(section)
            store.save(section, articles)
            articles
        } catch (e: Exception) {
            log.warn("Could not load '{}' from NYT, serving saved stories", section)
            if (fetchedAt == null) throw NewsUnavailableException(section)
            store.articles(section)
        }
    }
}

class UnknownSectionException(section: String) : Exception("Unknown section '$section'")

class NewsUnavailableException(section: String) : Exception("No data available for '$section'")
