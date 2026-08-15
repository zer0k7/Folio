package com.ghost.folio.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ghost.folio.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT COUNT(*) FROM reading_history")
    fun getHistoryCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordRead(history: HistoryEntity)

    @Query("DELETE FROM reading_history")
    suspend fun clearHistory()
}
