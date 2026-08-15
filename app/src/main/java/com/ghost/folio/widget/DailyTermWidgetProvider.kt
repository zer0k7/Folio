package com.ghost.folio.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DailyTermWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        DailyTermWidgetWorker.scheduleDailyUpdate(context)
        CoroutineScope(Dispatchers.IO).launch {
            DailyTermWidgetWorker.updateWidget(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        DailyTermWidgetWorker.scheduleDailyUpdate(context)
        CoroutineScope(Dispatchers.IO).launch {
            DailyTermWidgetWorker.updateWidget(context)
        }
    }

    companion object {
        const val EXTRA_ARTICLE_ID = "EXTRA_ARTICLE_ID"
    }
}
