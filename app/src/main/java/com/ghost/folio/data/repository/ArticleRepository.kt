package com.ghost.folio.data.repository

import com.ghost.folio.data.local.dao.ArticleDao
import com.ghost.folio.data.local.dao.CategoryDao
import com.ghost.folio.data.local.entity.ArticleEntity
import com.ghost.folio.data.local.entity.CategoryEntity
import com.ghost.folio.data.model.Article
import com.ghost.folio.data.model.Category
import com.ghost.folio.data.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ArticleRepository(
    private val articleDao: ArticleDao,
    private val categoryDao: CategoryDao
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

        val bodyBlocks = try {
            json.decodeFromString(bodyJson)
        } catch (_: Exception) {
            emptyList()
        }

        val tagsList = try {
            json.decodeFromString(tagsJson)
        } catch (_: Exception) {
            emptyList()
        }

        val relatedList = try {
            json.decodeFromString(relatedIdsJson)
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
            lastUpdated = lastUpdated,
            difficulty = parsedDifficulty,
            hasDiagram = hasDiagram,
            isSaved = isSaved
        )
    }
}
