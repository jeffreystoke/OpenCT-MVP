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

import android.graphics.Color
import android.text.TextUtils
import cc.metapro.openct.utils.REHelper
import java.util.*


class Classes : ArrayList<EnrichedClassInfo>() {

    override fun add(element: EnrichedClassInfo): Boolean {
        if (REHelper.isEmpty(element.name!!))
            return false
        var targetInfo: EnrichedClassInfo? = null

        for (c in this) {
            if (c == element) {
                targetInfo = c
                break
            }
        }

        if (targetInfo != null) {
            super.remove(targetInfo)
            targetInfo.combine(element)
            super.add(0, targetInfo)
        } else {
            super.add(0, element)
        }
        return true
    }

    override fun add(index: Int, element: EnrichedClassInfo) {
        add(element)
    }

    fun setInfoByName(name: String, newInfo: EnrichedClassInfo): Boolean {
        var target: EnrichedClassInfo? = null
        for (info in this) {
            val s = info.name
            if (TextUtils.isEmpty(s) && s.equals(name, ignoreCase = true)) {
                target = info
                break
            }
        }

        if (target != null) {
            remove(target)
            return add(newInfo)
        }
        return false
    }

    fun getTodayClasses(week: Int): List<SingleClass> {
        val todayClasses = ArrayList<SingleClass>()
        for (info in this) {
            val timeList = info.hasClassToday(week)
            if (!timeList.isEmpty()) {
                for (time in timeList) {
                    val place = time.place
                    val teacher = time.teacher
                    todayClasses.add(SingleClass(info.name!!, info.type!!, time, place, teacher, info.color))
                }
            }
        }
        Collections.sort(todayClasses)
        return todayClasses
    }

    fun getWeekClasses(week: Int): List<SingleClass> {
        if (week < 0 || week > 30) return allClasses

        val weekClasses = ArrayList<SingleClass>()
        val notWeekClasses = ArrayList<SingleClass>()
        for (info in this) {
            val weekTimeSet = info.hasClassThisWeek(week)
            if (!weekTimeSet.isEmpty()) {
                for (time in weekTimeSet) {
                    val place = time.place
                    val teacher = time.teacher
                    weekClasses.add(SingleClass(info.name!!, info.type!!, time, place, teacher, info.color))
                }

                // 添加本周有安排但是没有完全安排的课
                val allTimeSet = info.timeSet
                if (weekTimeSet.size < allTimeSet.size) {
                    for (time in allTimeSet) {
                        if (!weekTimeSet.contains(time)) {
                            val place = time.place
                            val teacher = time.teacher
                            notWeekClasses.add(SingleClass(info.name!!, info.type!!, time, place, teacher, Color.LTGRAY))
                        }
                    }
                }
            } else {
                // 添加本周没有安排的课
                for (time in info.timeSet) {
                    val place = time.place
                    val teacher = time.teacher
                    notWeekClasses.add(SingleClass(info.name!!, info.type!!, time, place, teacher, Color.LTGRAY))
                }
            }
        }

        // ensure unique class
        val result = ArrayList(weekClasses)
        for (a in notWeekClasses) {
            var found = false
            for (b in weekClasses) {
                if (a.classTime == b.classTime) {
                    found = true
                    break
                }
            }

            if (!found) {
                result.add(a)
            }
        }
        return result
    }

    private val allClasses: List<SingleClass>
        get() {
            val todayClasses = ArrayList<SingleClass>()
            for (info in this) {
                val timeList = info.timeSet
                if (!timeList.isEmpty()) {
                    for (time in timeList) {
                        val place = time.place
                        val teacher = time.teacher
                        todayClasses.add(SingleClass(info.name!!, info.type!!, time, place, teacher, info.color))
                    }
                }
            }
            return todayClasses
        }
}
