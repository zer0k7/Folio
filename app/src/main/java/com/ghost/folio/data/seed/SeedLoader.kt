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
import java.util.Locale

object SeedLoader {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private fun formatCategoryLabel(slug: String): String {
        return when (slug) {
            "ai-ml" -> "AI & Machine Learning"
            "app-dev" -> "App Development"
            "ports-cables" -> "Ports & Cables"
            "clocks-time" -> "Clocks & Time"
            "version-control" -> "Version Control"
            "display-advanced" -> "Advanced Displays"
            "os" -> "Operating Systems"
            "protocols" -> "Protocols & Standards"
            "formats" -> "File Formats"
            "devtools" -> "Developer Tools"
            "cybersecurity" -> "Cybersecurity"
            "ui-ux" -> "UI & UX Design"
            "soc" -> "System on Chip"
            else -> slug.split("-", "_").joinToString(" ") { word ->
                word.replaceFirstChar { char ->
                    if (char.isLowerCase()) char.titlecase(Locale.ROOT) else char.toString()
                }
            }
        }
    }

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
                val derivedCategorySlug = fileName.removeSuffix(".json")
                try {
                    val jsonString = assetManager.open("seed/$fileName").bufferedReader().use { it.readText() }
                    val articles: List<Article> = json.decodeFromString(jsonString)

                    for (article in articles) {
                        val categorySlug = article.category.ifBlank { derivedCategorySlug }
                        val entity = ArticleEntity(
                            id = article.id,
                            title = article.title,
                            categorySlug = categorySlug,
                            summary = article.summary,
                            bodyJson = json.encodeToString(article.body),
                            tagsJson = json.encodeToString(article.tags),
                            relatedIdsJson = json.encodeToString(article.relatedIds),
                            relatedLinksJson = json.encodeToString(article.relatedLinks),
                            lastUpdated = article.lastUpdated,
                            difficulty = article.difficulty.name,
                            hasDiagram = article.hasDiagram,
                            isSaved = false
                        )
                        articlesToInsert.add(entity)
                        categoryCounts[categorySlug] = (categoryCounts[categorySlug] ?: 0) + 1
                    }
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.d("SeedLoader", "Failed to parse seed/$fileName: ${e.message}")
                    }
                }
            }

            if (articlesToInsert.isNotEmpty()) {
                articleDao.upsertAll(articlesToInsert)

                val categoriesToInsert = categoryCounts.map { (slug, count) ->
                    CategoryEntity(
                        slug = slug,
                        label = formatCategoryLabel(slug),
                        articleCount = count
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
