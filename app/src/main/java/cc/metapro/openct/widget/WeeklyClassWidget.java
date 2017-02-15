package cc.metapro.openct.widget;

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

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.RemoteViews;

import java.io.FileNotFoundException;

import cc.metapro.openct.R;

import static cc.metapro.openct.widget.DailyClassWidget.UPDATE_ITEMS;

public class WeeklyClassWidget extends AppWidgetProvider {

    public static final String WEEKLY_PIC_NAME = "weekly_pic";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_weekly_class);
        views.setEmptyView(R.id.classes, R.id.empty_view);
        try {
            views.setImageViewBitmap(R.id.classes, BitmapFactory.decodeStream(context.openFileInput(WEEKLY_PIC_NAME)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        ComponentName component = new ComponentName(context, DailyClassWidget.class);
        int[] ids = manager.getAppWidgetIds(component);
        switch (action) {
            case UPDATE_ITEMS:
            case Intent.ACTION_TIME_CHANGED:
            case Intent.ACTION_BOOT_COMPLETED:
            case Intent.ACTION_DATE_CHANGED:
            case Intent.ACTION_TIME_TICK:
            case Intent.ACTION_USER_PRESENT:
                onUpdate(context, manager, ids);
                break;
        }

        super.onReceive(context, intent);
    }
}

