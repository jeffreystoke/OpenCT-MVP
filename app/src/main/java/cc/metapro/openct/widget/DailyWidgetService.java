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

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.model.classinfo.SingleClass;

public class DailyWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new DailyWidgetFactory(getApplicationContext(), intent);
    }

    private static class DailyWidgetFactory implements RemoteViewsFactory {

        @NonNull
        private List<SingleClass> mDailyClasses = new ArrayList<>();
        private Context mContext;

        DailyWidgetFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
            DBManger manger = DBManger.getInstance(mContext);
            int week = LocalHelper.getCurrentWeek(mContext);
            mDailyClasses = manger.getClasses().getTodayClasses(week);
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return mDailyClasses.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            if (i < 0 || i >= getCount()) {
                return null;
            }
            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_list);
            SingleClass classInfo = mDailyClasses.get(i);

            views.setTextViewText(R.id.widget_class_name, classInfo.getName());
            views.setTextViewText(R.id.widget_class_type, classInfo.getType());
            views.setTextViewText(R.id.widget_class_place,
                    mContext.getString(R.string.text_today_seq, classInfo.getTimeString()) + ", " +
                            mContext.getString(R.string.text_place_at, classInfo.getPlace()));
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
