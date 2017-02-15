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
import android.support.annotation.Keep;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;

@Keep
public class DailyWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetFactory(getApplicationContext(), intent);
    }

    static class WidgetFactory implements RemoteViewsFactory {

        private static List<ClassInfo> mDailyClasses;
        private Context mContext;

        WidgetFactory(Context context, Intent intent) {
            mContext = context;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            DBManger manger = DBManger.getInstance(mContext);
            int week = Loader.getCurrentWeek(mContext);
            List<EnrichedClassInfo> allClasses = manger.getClasses();
            mDailyClasses = new ArrayList<>(0);
            if (allClasses.size() != 0) {
                for (EnrichedClassInfo info : allClasses) {
                    if (info.isToday()) {
                        List<ClassInfo> classes = info.getAllClasses();
                        for (ClassInfo c : classes) {
                            if (c.hasClass(week)) {
                                mDailyClasses.add(c);
                            }
                        }
                    }
                }
            }
            Collections.sort(mDailyClasses);
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
            ClassInfo classInfo = mDailyClasses.get(i);

            views.setTextViewText(R.id.widget_class_name, classInfo.getName());
            views.setTextViewText(R.id.widget_class_place, classInfo.getTime() + " 节 在 " + classInfo.getPlace());
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
