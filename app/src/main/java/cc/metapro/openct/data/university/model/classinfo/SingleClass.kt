package cc.metapro.openct.data.university.model.classinfo

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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import cc.metapro.openct.R
import cc.metapro.openct.classdetail.ClassDetailActivity
import cc.metapro.openct.utils.Constants

class SingleClass internal constructor(val name: String, val type: String, val classTime: ClassTime, val place: String, val teacher: String, private val color: Int) : Comparable<SingleClass>, View.OnClickListener {

    /**
     * Get Time in String, actually it is sequence of class daily
     * for example 3 - 4 or 3
     */
    val timeString: String
        get() = classTime.timeString

    fun inSameWeek(week: Int): Boolean {
        return classTime.hasClass(week)
    }

    fun addViewTo(container: ViewGroup, inflater: LayoutInflater) {
        val x = (classTime.weekDay - 1) * Constants.CLASS_WIDTH
        val y = (classTime.dailySeq - 1) * Constants.CLASS_BASE_HEIGHT
        val N = container.childCount
        for (i in 0..N - 1) {
            val childX = container.getChildAt(i).x.toInt()
            val childY = container.getChildAt(i).y.toInt()
            if (childX == x && childY == y) {
                return
            }
        }

        val card = inflater.inflate(R.layout.item_class_info, container, false) as TextView
        card.setBackgroundColor(color)

        val textView = card.findViewById<View>(R.id.class_name) as TextView
        var length = classTime.length
        if (length > 5 || length < 1) {
            length = 1
            textView.text = name
        } else {
            textView.text = name + "@" + classTime.place
        }

        card.x = x.toFloat()
        card.y = y.toFloat()
        container.addView(card)
        card.layoutParams.height = length * Constants.CLASS_BASE_HEIGHT
        card.layoutParams.width = Constants.CLASS_WIDTH
        card.setOnClickListener(this)
    }

    override fun compareTo(o: SingleClass): Int {
        return classTime.compareTo(o.classTime)
    }

    override fun onClick(v: View) {
        ClassDetailActivity.actionStart(v.context, name)
    }
}
