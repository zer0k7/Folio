package com.ghost.folio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ghost.folio.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE categorySlug = :slug")
    fun getByCategory(slug: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE isSaved = 1")
    fun getSaved(): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles WHERE title LIKE '%' || :q || '%' OR summary LIKE '%' || :q || '%'")
    fun search(q: String): Flow<List<ArticleEntity>>

    @Query("SELECT * FROM articles")
    fun getAll(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("UPDATE articles SET isSaved = :saved WHERE id = :id")
    suspend fun setSaved(id: String, saved: Boolean)

    @Query("SELECT COUNT(*) FROM articles")
    suspend fun count(): Int

    @Query("SELECT * FROM articles LIMIT 1 OFFSET :offset")
    suspend fun getArticleByOffset(offset: Int): ArticleEntity?

    @Query("SELECT * FROM articles LIMIT 1")
    suspend fun getFirstArticle(): ArticleEntity?
}
