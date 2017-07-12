package cc.metapro.openct.classdetail

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
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.ClassTime
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.ReferenceUtils
import com.rengwuxian.materialedittext.MaterialEditText

internal class ClassDetailAdapter(context: Context, private val mClassTimes: MutableList<ClassTime>?) : RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassDetailViewHolder {
        val view = mInflater.inflate(R.layout.item_class_detail, parent, false)
        return ClassDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClassDetailViewHolder, position: Int) {
        holder.setInfo(position)
    }

    override fun getItemCount(): Int {
        return mClassTimes?.size ?: 0
    }

    internal inner class ClassDetailViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.week_day)
        var mWeekDay: Spinner? = null
        @BindView(R.id.time_start)
        var mTimeStart: MaterialEditText? = null
        @BindView(R.id.time_end)
        var mTimeEnd: MaterialEditText? = null
        @BindView(R.id.during_container)
        var mDuringContainer: LinearLayout? = null
        var selections: Array<TextView?> = arrayOfNulls(Constants.WEEKS)
        @BindView(R.id.class_teacher)
        var mTeacher: MaterialEditText? = null
        @BindView(R.id.class_place)
        var mPlace: MaterialEditText? = null
        @BindView(R.id.edit)
        var mEdit: TextView? = null

        var posit = 0

        init {
            ButterKnife.bind(this, itemView)
        }

        private fun setEditable(isEditable: Boolean) {
            mWeekDay!!.isEnabled = isEditable
            mTimeStart!!.isEnabled = isEditable
            mTimeEnd!!.isEnabled = isEditable
            mTeacher!!.isEnabled = isEditable
            mPlace!!.isEnabled = isEditable
            for (textView in selections) {
                textView!!.isEnabled = isEditable
            }
            if (isEditable) {
                mEdit!!.setText(R.string.save)
                mEdit!!.setOnClickListener {
                    setEditable(false)
                    var time = mClassTimes!![posit]
                    mClassTimes.remove(time)
                    time = getInfo(time)
                    mClassTimes.add(posit, time)
                }
            } else {
                mEdit!!.setText(R.string.edit)
                mEdit!!.setOnClickListener { setEditable(true) }
            }
        }

        // set info and react to during click
        fun setInfo(position: Int) {
            this.posit = position
            val time = mClassTimes!![position]
            mTimeStart!!.setText("" + time.dailySeq)
            mTimeEnd!!.setText("" + time.dailyEnd)
            mWeekDay!!.setSelection(time.weekDay - 1, true)
            mTeacher!!.setText(time.teacher)
            mPlace!!.setText(time.place)

            val context = mDuringContainer!!.context

            mDuringContainer!!.removeAllViews()
            for (i in 0..Constants.WEEKS / 6 - 1) {
                val linearLayout = LinearLayout(mDuringContainer!!.context)
                linearLayout.orientation = LinearLayout.HORIZONTAL

                mDuringContainer!!.addView(linearLayout)
                val params = linearLayout.layoutParams
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT
                params.width = ViewGroup.LayoutParams.MATCH_PARENT
                linearLayout.layoutParams = params

                for (j in 0..5) {
                    val week = i * 6 + j + 1
                    val textView = TextView(context)
                    textView.text = week.toString() + ""
                    textView.gravity = Gravity.CENTER
                    if (time.hasClass(week)) {
                        textView.background = ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue)
                        textView.setTextColor(ReferenceUtils.getThemeColor(context, R.attr.colorAccent))
                    } else {
                        textView.background = ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey)
                        textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey))
                    }

                    textView.setOnClickListener {
                        mClassTimes.remove(time)
                        if (time.hasClass(week)) {
                            textView.background = ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey)
                            textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey))
                            time.disableWeek(week)
                        } else {
                            textView.background = ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue)
                            textView.setTextColor(ReferenceUtils.getThemeColor(context, R.attr.colorAccent))
                            time.enableWeek(week)
                        }
                        mClassTimes.add(position, time)
                    }
                    textView.setLines(1)
                    linearLayout.addView(textView, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
                    selections[week - 1] = textView
                }
            }
            setEditable(false)
        }

        fun getInfo(oldTime: ClassTime): ClassTime {
            val weekDay = mWeekDay!!.selectedItemPosition + 1
            val dailySeq = Integer.parseInt(mTimeStart!!.text.toString())
            val dailyEnd = Integer.parseInt(mTimeEnd!!.text.toString())

            oldTime.place = mPlace!!.text.toString()
            oldTime.teacher = mTeacher!!.text.toString()
            oldTime.dailySeq = dailySeq
            oldTime.weekDay = weekDay
            oldTime.dailyEnd = dailyEnd
            return oldTime
        }
    }
}
