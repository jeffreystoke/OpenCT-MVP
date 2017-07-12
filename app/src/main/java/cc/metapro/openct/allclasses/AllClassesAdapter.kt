package cc.metapro.openct.allclasses

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

import android.app.FragmentManager
import android.support.annotation.ColorInt
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.classdetail.ClassDetailActivity
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.DateHelper
import cc.metapro.openct.utils.REHelper
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import java.util.*


internal class AllClassesAdapter(activity: AppCompatActivity) : RecyclerView.Adapter<AllClassesAdapter.ClassViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(activity)
    private val mFragmentManager: FragmentManager = activity.fragmentManager

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClassViewHolder {
        return ClassViewHolder(mInflater.inflate(R.layout.item_class_all, parent, false))
    }

    override fun onBindViewHolder(holder: ClassViewHolder, position: Int) {
        holder.setInfo(Constants.sClasses[position], mFragmentManager, position)
    }

    override fun getItemCount(): Int {
        return Constants.sClasses.size
    }

    internal class ClassViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.color)
        var mColor: TextView? = null
        @BindView(R.id.content)
        var mName: TextView? = null
        @BindView(R.id.time)
        var mTime: TextView? = null
        @BindView(R.id.edit)
        var mEdit: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun setInfo(info: EnrichedClassInfo, manager: FragmentManager, position: Int) {
            mColor!!.setBackgroundColor(info.color)
            mName!!.text = info.name + "   " + info.type
            var time = ""
            val tmpTimeList = ArrayList(info.timeSet)
            Collections.sort(tmpTimeList)
            for (t in tmpTimeList) {
                val tmp = DateHelper.weekDayTrans(mName!!.context, t.weekDay) + " " + t.timeString + " , "
                if (!time.contains(tmp)) {
                    time += tmp
                }
            }
            if (!REHelper.isEmpty(time)) {
                time = time.substring(0, time.length - 2)
            }

            mTime!!.text = time
            mColor!!.setOnClickListener {
                val dialog = ColorPickerDialog.newBuilder().setColor(info.color).create()
                dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
                    override fun onColorSelected(dialogId: Int, @ColorInt color: Int) {
                        mColor!!.setBackgroundColor(color)
                        Constants.sClasses[position].color = color
                    }

                    override fun onDialogDismissed(dialogId: Int) {

                    }
                })
                dialog.show(manager, "color_picker")
            }
            mEdit!!.setOnClickListener { ClassDetailActivity.actionStart(mName!!.context, info.name!!) }
        }
    }
}
