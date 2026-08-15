package com.ghost.folio.data.repository

import com.ghost.folio.data.local.dao.ArticleDao
import com.ghost.folio.data.local.dao.CategoryDao
import com.ghost.folio.data.local.entity.ArticleEntity
import com.ghost.folio.data.local.entity.CategoryEntity
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.BodyBlock
import com.ghost.folio.data.model.Category
import com.ghost.folio.data.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

import com.ghost.folio.data.local.dao.HistoryDao
import com.ghost.folio.data.local.entity.HistoryEntity

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val categoryDao: CategoryDao,
    private val historyDao: HistoryDao
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    fun getAllArticles(): Flow<List<Article>> {
        return articleDao.getAll().map { list -> list.map { it.toDomain() } }
    }

    fun getArticlesByCategory(categorySlug: String): Flow<List<Article>> {
        return articleDao.getByCategory(categorySlug).map { list -> list.map { it.toDomain() } }
    }

    suspend fun getArticleById(id: String): Article? {
        return articleDao.getById(id)?.toDomain()
    }

    fun getSavedArticles(): Flow<List<Article>> {
        return articleDao.getSaved().map { list -> list.map { it.toDomain() } }
    }

    fun searchArticles(query: String): Flow<List<Article>> {
        return articleDao.search(query).map { list -> list.map { it.toDomain() } }
    }

    suspend fun setArticleSaved(id: String, saved: Boolean) {
        articleDao.setSaved(id, saved)
    }

    fun getReadingHistoryCount(): Flow<Int> {
        return historyDao.getHistoryCount()
    }

    suspend fun recordArticleRead(articleId: String) {
        historyDao.recordRead(HistoryEntity(articleId = articleId))
    }

    suspend fun clearReadingHistory() {
        historyDao.clearHistory()
    }

    fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAll().map { list ->
            list.map { Category(slug = it.slug, label = it.label, articleCount = it.articleCount) }
        }
    }

    private fun ArticleEntity.toDomain(): Article {
        val parsedDifficulty = try {
            Difficulty.valueOf(difficulty.uppercase())
        } catch (_: Exception) {
            Difficulty.BASIC
        }

        val bodyBlocks: List<BodyBlock> = try {
            json.decodeFromString<List<BodyBlock>>(bodyJson)
        } catch (_: Exception) {
            emptyList()
        }

        val tagsList: List<String> = try {
            json.decodeFromString<List<String>>(tagsJson)
        } catch (_: Exception) {
            emptyList()
        }

        val relatedList: List<String> = try {
            json.decodeFromString<List<String>>(relatedIdsJson)
        } catch (_: Exception) {
            emptyList()
        }

        val relatedLinksList: List<com.ghost.folio.data.model.RelatedLink> = try {
            json.decodeFromString<List<com.ghost.folio.data.model.RelatedLink>>(relatedLinksJson)
        } catch (_: Exception) {
            emptyList()
        }

        return Article(
            id = id,
            title = title,
            category = categorySlug,
            summary = summary,
            body = bodyBlocks,
            tags = tagsList,
            relatedIds = relatedList,
            relatedLinks = relatedLinksList,
            lastUpdated = lastUpdated,
            difficulty = parsedDifficulty,
            hasDiagram = hasDiagram,
            isSaved = isSaved
        )
    }
}
