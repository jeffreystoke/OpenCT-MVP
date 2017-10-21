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

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.DBManger
import cc.metapro.openct.data.source.LocalHelper
import cc.metapro.openct.data.university.model.classinfo.SingleClass
import java.util.*

class DailyWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return DailyWidgetFactory(applicationContext)
    }

    private class DailyWidgetFactory internal constructor(private val mContext: Context) : RemoteViewsService.RemoteViewsFactory {

        private var mDailyClasses: List<SingleClass> = ArrayList()

        override fun onCreate() {

        }

        override fun onDataSetChanged() {
            val manger = DBManger.getInstance(mContext)
            val week = LocalHelper.getCurrentWeek(mContext)
            mDailyClasses = manger!!.classes.getTodayClasses(week)
        }

        override fun onDestroy() {}

        override fun getCount(): Int {
            return mDailyClasses.size
        }

        override fun getViewAt(i: Int): RemoteViews? {
            if (i < 0 || i >= count) {
                return null
            }
            val views = RemoteViews(mContext.packageName, R.layout.item_widget_list)
            val classInfo = mDailyClasses[i]

            views.setTextViewText(R.id.widget_class_name, classInfo.name)
            views.setTextViewText(R.id.widget_class_type, classInfo.type)
            views.setTextViewText(R.id.widget_class_place,
                    mContext.getString(R.string.text_today_seq, classInfo.timeString) + ", " +
                            mContext.getString(R.string.text_place_at, classInfo.place))
            return views
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun getItemId(i: Int): Long {
            return i.toLong()
        }

        override fun hasStableIds(): Boolean {
            return false
        }
    }
}
