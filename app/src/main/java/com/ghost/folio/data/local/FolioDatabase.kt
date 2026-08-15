package com.ghost.folio.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ghost.folio.data.local.converter.Converters
import com.ghost.folio.data.local.dao.ArticleDao
import com.ghost.folio.data.local.dao.CategoryDao
import com.ghost.folio.data.local.dao.HistoryDao
import com.ghost.folio.data.local.entity.ArticleEntity
import com.ghost.folio.data.local.entity.CategoryEntity
import com.ghost.folio.data.local.entity.HistoryEntity

@Database(
    entities = [ArticleEntity::class, CategoryEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FolioDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
    abstract fun categoryDao(): CategoryDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: FolioDatabase? = null

        fun getDatabase(context: Context): FolioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FolioDatabase::class.java,
                    "folio_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
