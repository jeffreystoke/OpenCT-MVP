package cc.metapro.openct.widget

/*
 *  Copyright 2016 - 2017 OpenCT open source class table
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews

import cc.metapro.openct.R

class DailyClassWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, DailyClassWidget::class.java)
        val ids = manager.getAppWidgetIds(component)
        when (action) {
            UPDATE_ITEMS, Intent.ACTION_TIME_CHANGED, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_TICK, Intent.ACTION_USER_PRESENT -> onUpdate(context, manager, ids)
        }

        super.onReceive(context, intent)
    }

    companion object {

        val UPDATE_ITEMS = "cc.metapro.openct.action.UPDATE_ITEMS"

        fun update(context: Context) {
            val intent = Intent(UPDATE_ITEMS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.sendBroadcast(intent)
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val intent = Intent(context, DailyWidgetService::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            intent.data = Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME))

            val views = RemoteViews(context.packageName, R.layout.widget_daily_class)
            views.setRemoteAdapter(R.id.widget_list_view, intent)
            views.setEmptyView(R.id.widget_list_view, R.id.empty_view)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
        }
    }

}

