package com.ghost.folio.data.seed

import android.content.Context
import android.util.Log
import com.ghost.folio.BuildConfig
import com.ghost.folio.data.local.FolioDatabase
import com.ghost.folio.data.local.entity.ArticleEntity
import com.ghost.folio.data.local.entity.CategoryEntity
import com.ghost.folio.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object SeedLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val categoryMetadata = mapOf(
        "display" to "Displays",
        "android" to "Android",
        "networking" to "Networking",
        "hardware" to "Hardware",
        "storage" to "Storage",
        "security" to "Security",
        "os" to "Operating Systems",
        "web" to "Web",
        "programming" to "Programming",
        "electronics" to "Electronics",
        "audio" to "Audio",
        "camera" to "Camera",
        "gaming" to "Gaming",
        "formats" to "File Formats",
        "protocols" to "Protocols & Standards"
    )

    suspend fun seedIfNeeded(context: Context, database: FolioDatabase) = withContext(Dispatchers.IO) {
        try {
            val articleDao = database.articleDao()
            val categoryDao = database.categoryDao()

            if (articleDao.count() > 0) {
                return@withContext
            }

            val assetManager = context.assets
            val seedFileNames = try {
                assetManager.list("seed")?.filter { it.endsWith(".json") } ?: emptyList()
            } catch (e: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.d("SeedLoader", "Failed to list seed directory: ${e.message}")
                }
                emptyList()
            }

            val articlesToInsert = mutableListOf<ArticleEntity>()
            val categoryCounts = mutableMapOf<String, Int>()

            for (fileName in seedFileNames) {
                try {
                    val jsonString = assetManager.open("seed/$fileName").bufferedReader().use { it.readText() }
                    val articles: List<Article> = json.decodeFromString(jsonString)

                    for (article in articles) {
                        val entity = ArticleEntity(
                            id = article.id,
                            title = article.title,
                            categorySlug = article.category,
                            summary = article.summary,
                            bodyJson = json.encodeToString(article.body),
                            tagsJson = json.encodeToString(article.tags),
                            relatedIdsJson = json.encodeToString(article.relatedIds),
                            lastUpdated = article.lastUpdated,
                            difficulty = article.difficulty.name,
                            hasDiagram = article.hasDiagram,
                            isSaved = false
                        )
                        articlesToInsert.add(entity)
                        categoryCounts[article.category] = (categoryCounts[article.category] ?: 0) + 1
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.d("SeedLoader", "Failed to parse seed/$fileName: ${e.message}")
                    }
                }
            }

            if (articlesToInsert.isNotEmpty()) {
                articleDao.upsertAll(articlesToInsert)

                val categoriesToInsert = categoryMetadata.map { (slug, label) ->
                    CategoryEntity(
                        slug = slug,
                        label = label,
                        articleCount = categoryCounts[slug] ?: 0
                    )
                }
                categoryDao.upsertAll(categoriesToInsert)
            }
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.d("SeedLoader", "Seed loading failed entirely: ${e.message}")
            }
        }
    }
}
