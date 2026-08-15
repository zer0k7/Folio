package com.ghost.folio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val slug: String,
    val label: String,
    val articleCount: Int
)
