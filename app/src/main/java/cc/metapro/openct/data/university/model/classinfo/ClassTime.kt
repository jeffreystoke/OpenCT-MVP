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

import android.text.TextUtils
import cc.metapro.openct.data.university.ClassTableInfo
import cc.metapro.openct.utils.ClassInfoHelper
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.DateHelper
import java.util.*

class ClassTime : Comparable<ClassTime> {

    var weekDay = 1
    var dailySeq = 1
    var dailyEnd = 1

    var teacher = ""
    var place = ""

    internal var during: BooleanArray? = BooleanArray(Constants.WEEKS)
        private set

    constructor(classTime: ClassTime) {
        place = classTime.place
        teacher = classTime.teacher
    }

    constructor() {

    }

    internal constructor(weekDay: Int, dailySeq: Int, dailyEnd: Int, teacher: String, place: String, weeks: BooleanArray) {
        this.weekDay = weekDay
        this.dailySeq = dailySeq
        this.dailyEnd = dailyEnd
        this.teacher = teacher
        this.place = place
        this.during = weeks
    }

    internal constructor(weekDay: Int, dailySeq: Int, contents: Array<String>, info: ClassTableInfo) : this(ClassInfoHelper.infoParser(info.mTimeIndex, info.mTimeRE, contents), weekDay, dailySeq) {
        teacher = ClassInfoHelper.infoParser(info.mTeacherIndex, info.mTeacherRE, contents)
        place = ClassInfoHelper.infoParser(info.mPlaceIndex, info.mPlaceRE, contents)

        var rawDuring = ""
        if (info.mDuringIndex < contents.size && info.mDuringIndex >= 0) {
            rawDuring = contents[info.mDuringIndex]
        }

        during = ClassInfoHelper.combineDuring(info.mDuringRE, rawDuring, during)
    }

    private constructor(time: String, weekDay: Int, dailySeq: Int) {
        var length = ClassInfoHelper.getLength(time)
        if (length <= 0 || length > 5) {
            length = 1
        }
        this.dailySeq = dailySeq
        if (this.dailySeq <= 0) {
            this.dailySeq = 1
        }
        this.weekDay = weekDay
        this.dailyEnd = dailySeq + length - 1
    }

    fun hasClass(week: Int): Boolean {
        if (during == null) {
            during = BooleanArray(Constants.WEEKS)
        }
        return week > 0 && week <= Constants.WEEKS && during!![week - 1]
    }

    val length: Int
        get() = dailyEnd - dailySeq + 1

    val timeString: String
        get() {
            if (dailyEnd - dailySeq <= 0) {
                return dailySeq.toString() + ""
            } else {
                return dailySeq.toString() + " - " + dailyEnd
            }
        }

    internal fun inSameDay(time: ClassTime?): Boolean {
        return time != null && time.weekDay == weekDay
    }

    internal fun inSameDay(calendar: Calendar): Boolean {
        return DateHelper.weekDayConvert(weekDay) == calendar.get(Calendar.DAY_OF_WEEK)
    }

    fun enableWeek(week: Int) {
        if (during == null) {
            during = BooleanArray(Constants.WEEKS)
        }
        if (week > 0 && week <= Constants.WEEKS) {
            during[week - 1] = true
        }
    }

    fun disableWeek(week: Int) {
        if (during == null) {
            during = BooleanArray(Constants.WEEKS)
        }
        if (week > 0 && week <= Constants.WEEKS) {
            during[week - 1] = false
        }
    }

    internal fun combineDuring(during: BooleanArray?) {
        if (during == null) return
        for (i in during.indices) {
            if (during[i]) {
                this.during[i] = true
            }
        }
    }

    override fun equals(obj: Any?): Boolean {
        if (obj === this) {
            return true
        } else if (obj is ClassTime) {
            val time = obj as ClassTime?
            return time!!.weekDay == weekDay
                    && time.dailySeq == dailySeq
                    && TextUtils.isEmpty(place) && place == time.place
                    && TextUtils.isEmpty(teacher) && teacher == time.teacher
        }
        return false
    }

    override fun compareTo(o: ClassTime): Int {
        if (weekDay == o.weekDay) {
            if (dailySeq < o.dailySeq) {
                return -1
            } else {
                return 1
            }
        } else if (weekDay < o.weekDay) {
            return -1
        } else if (weekDay > o.weekDay) {
            return 1
        }

        return 0
    }

}
