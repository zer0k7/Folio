package com.ghost.folio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val categorySlug: String,
    val summary: String,
    val bodyJson: String,
    val tagsJson: String,
    val relatedIdsJson: String,
    val relatedLinksJson: String = "[]",
    val lastUpdated: String,
    val difficulty: String,
    val hasDiagram: Boolean,
    val isSaved: Boolean = false
)
