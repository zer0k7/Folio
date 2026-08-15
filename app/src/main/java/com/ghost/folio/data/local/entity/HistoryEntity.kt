package com.ghost.folio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reading_history")
data class HistoryEntity(
    @PrimaryKey val articleId: String,
    val readTimestamp: Long = System.currentTimeMillis()
)
