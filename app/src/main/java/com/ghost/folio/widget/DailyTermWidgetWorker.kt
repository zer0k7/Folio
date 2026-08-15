package com.ghost.folio.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ghost.folio.MainActivity
import com.ghost.folio.R
import com.ghost.folio.data.local.FolioDatabase
import com.ghost.folio.data.model.BodyBlock
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.concurrent.TimeUnit

class DailyTermWidgetWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    override suspend fun doWork(): Result {
        updateWidget(context)
        return Result.success()
    }

    companion object {
        const val WORK_TAG = "daily_term_widget_update"

        fun scheduleDailyUpdate(context: Context) {
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 1)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val initialDelayMs = (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

            val workRequest = PeriodicWorkRequestBuilder<DailyTermWidgetWorker>(24, TimeUnit.HOURS)
                .setInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS)
                .addTag(WORK_TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        suspend fun updateWidget(context: Context) {
            try {
                val database = FolioDatabase.getDatabase(context)
                val articleDao = database.articleDao()
                val count = articleDao.count()

                val article = if (count > 0) {
                    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    val offset = (dayOfYear - 1) % count
                    articleDao.getArticleByOffset(offset) ?: articleDao.getFirstArticle()
                } else {
                    null
                }

                val title = article?.title ?: "Pixel"
                val category = (article?.categorySlug ?: "display").replace("-", " ").uppercase()
                val definitionText = article?.let { entity ->
                    try {
                        val bodyBlocks: List<BodyBlock> = Json.decodeFromString(entity.bodyJson)
                        val defBlock = bodyBlocks.filterIsInstance<BodyBlock.Definition>().firstOrNull()
                        val rawText = defBlock?.definition ?: entity.summary
                        rawText.split(". ").firstOrNull()?.let { if (it.endsWith(".")) it else "$it." } ?: rawText
                    } catch (_: Exception) {
                        entity.summary
                    }
                } ?: "The smallest controllable element in a raster display device."
                val articleId = article?.id ?: "what-is-pixel"

                val views = RemoteViews(context.packageName, R.layout.widget_daily_term).apply {
                    setTextViewText(R.id.widget_term_title, title)
                    setTextViewText(R.id.widget_term_definition, definitionText)
                    setTextViewText(R.id.widget_term_category, category)

                    val intent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(DailyTermWidgetProvider.EXTRA_ARTICLE_ID, articleId)
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                }

                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, DailyTermWidgetProvider::class.java)
                appWidgetManager.updateAppWidget(componentName, views)
            } catch (_: Exception) {
                // Gracefully maintain previous widget state
            }
        }
    }
}
