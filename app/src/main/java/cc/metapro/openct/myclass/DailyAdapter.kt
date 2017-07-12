package cc.metapro.openct.myclass

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
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.data.university.model.classinfo.SingleClass
import java.util.*

internal class DailyAdapter(context: Context) : RecyclerView.Adapter<DailyAdapter.ClassViewHolder>() {

    private var mTodayClasses: List<SingleClass> = ArrayList(0)
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        val view = mInflater.inflate(R.layout.item_class, parent, false)
        return ClassViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.setInfo(mTodayClasses[position])
    }

    override fun getItemCount(): Int {
        return mTodayClasses.size
    }

    fun updateTodayClasses(classes: Classes, week: Int) {
        mTodayClasses = classes.getTodayClasses(week)
    }

    fun hasClassToday(): Boolean {
        return mTodayClasses.size > 0
    }

    internal class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.class_name)
        var mClassName: TextView? = null
        @BindView(R.id.class_place_time)
        var mTimePlace: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun setInfo(info: SingleClass) {
            mClassName!!.text = info.name
            var content = ""

            if (!TextUtils.isEmpty(info.timeString)) {
                content += mClassName!!.context.getString(R.string.text_today_seq, info.timeString)
            }

            if (!TextUtils.isEmpty(info.place)) {
                if (!TextUtils.isEmpty(content)) {
                    content += ", "
                }
                content += mClassName!!.context.getString(R.string.text_place_at, info.place)
            }

            mTimePlace!!.text = content
        }
    }
}
