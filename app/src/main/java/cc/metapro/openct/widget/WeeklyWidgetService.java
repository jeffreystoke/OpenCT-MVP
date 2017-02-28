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
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.classinfo.SingleClass;

public class WeeklyWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WeeklyWidgetFactory(getApplicationContext(), intent);
    }

    private static class WeeklyWidgetFactory implements RemoteViewsFactory {

        private static List<SingleClass> mWeeklyClasses = new ArrayList<>();
        private Context mContext;

        WeeklyWidgetFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            DBManger manger = DBManger.getInstance(mContext);
            int week = Loader.getCurrentWeek(mContext);
            mWeeklyClasses = manger.getClasses().getWeekClasses(week);
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            if (mWeeklyClasses == null) return 0;
            return 0;
        }

        @Override
        public RemoteViews getViewAt(int i) {
//            SingleClass singleClass = mWeeklyClasses.get(i);
//            RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_class_info);
//            views.setTextViewText(R.id.class_info, singleClass.getName() + "@" + singleClass.getPlace());
            return null;
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
