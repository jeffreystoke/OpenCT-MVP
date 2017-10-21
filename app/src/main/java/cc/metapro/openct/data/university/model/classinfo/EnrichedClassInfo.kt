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
import android.support.v4.util.ArraySet
import android.text.TextUtils
import cc.metapro.openct.data.university.ClassTableInfo
import cc.metapro.openct.utils.CharacterParser
import java.util.*

/**
 * One EnrichedClassInfo Object has one ClassInfo Object
 */
class EnrichedClassInfo : Comparable<EnrichedClassInfo> {

    var name: String? = null

    var type: String? = null

    private var mTimeSet: MutableSet<ClassTime>? = ArraySet()

    var color = Color.parseColor("#968bc34a")

    constructor(name: String, type: String, time: ClassTime?) {
        this.name = name
        this.type = type
        if (time != null) {
            mTimeSet!!.add(time)
        }
    }

    /**
     * only one class info content should be there

     * @param content string class info content
     * *
     * @param weekday day of week
     * *
     * @param info    cms class table for class info generation
     * *
     * @param color   color of background, same classes share the same color
     */
    constructor(content: String, weekday: Int, dailySeq: Int, color: Int, info: ClassTableInfo) : this(content, weekday, dailySeq, info) {
        this.color = color
    }

    constructor(content: String, weekday: Int, dailySeq: Int, info: ClassTableInfo) {
//        val infoContents = content.split(HTMLUtils.BR_REPLACER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//        name = ClassInfoHelper.infoParser(info.mNameIndex, info.mNameRE, infoContents)
//        type = ClassInfoHelper.infoParser(info.mTypeIndex, info.mTypeRE, infoContents)
//        mTimeSet!!.add(ClassTime(weekday, dailySeq, infoContents, info))
    }

    /**
     * judge whether this class is on schedule in this week

     * @param week current week or selected week
     * *
     * @return time when has class this week
     */
    internal fun hasClassThisWeek(week: Int): Set<ClassTime> {
        val result = ArraySet<ClassTime>()
        for (time in mTimeSet!!) {
            if (time.hasClass(week)) {
                result.add(time)
            }
        }
        return result
    }

    /**
     * judge whether this class is on schedule today

     * @param week current week or selected week
     * *
     * @return time when has class today
     */
    internal fun hasClassToday(week: Int): Set<ClassTime> {
        val calendar = Calendar.getInstance()
        val timeList = hasClassThisWeek(week)
        val result = ArraySet<ClassTime>()
        for (time in timeList) {
            if (time.inSameDay(calendar)) {
                result.add(time)
            }
        }
        return result
    }

    /**
     * merge class with same name to one, the differences should be in time, place, teacher, during
     * and time is the key of them

     * @param info another class info which has the same name with this one
     */
    internal fun combine(info: EnrichedClassInfo?) {
        if (info == null) return
        val mixedTime = ArraySet<ClassTime>()
        mixedTime.addAll(mTimeSet!!)

        for (time in info.timeSet) {
            var found = false
            for (myTime in mTimeSet!!) {
                if (myTime == time) {
                    found = true
                    myTime.combineDuring(time.during)
                    mixedTime.add(myTime)
                }
            }
            if (!found) {
                mixedTime.add(time)
            }
        }
        mTimeSet = mixedTime
    }

    /**
     * get a set of class time, which includes daily time, during, place and teacher
     */
    val timeSet: Set<ClassTime>
        get() = mTimeSet!!

    fun setTimes(timeSet: MutableSet<ClassTime>?) {
        if (timeSet != null && !timeSet.isEmpty()) {
            mTimeSet = timeSet
        }
    }

    val isEmpty: Boolean
        get() = mTimeSet == null || mTimeSet!!.isEmpty()

    override fun toString(): String {
//        return if (isEmpty) "" else StoreHelper.toJson(this)
        return ""
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        } else if (obj is EnrichedClassInfo) {
            val classInfo = obj as EnrichedClassInfo?
            return !TextUtils.isEmpty(name) && name == classInfo!!.name
        }
        return false
    }

    override fun compareTo(o: EnrichedClassInfo): Int {
        val parser = CharacterParser.instance
        val me = parser.getSpelling(name!!)
        val that = parser.getSpelling(o.name!!)
        return me.compareTo(that)
    }

}
