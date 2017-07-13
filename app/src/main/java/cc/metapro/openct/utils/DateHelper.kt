package cc.metapro.openct.utils

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
import cc.metapro.openct.R
import java.util.*
import java.util.regex.Pattern

object DateHelper {

    private val MONDAY = "[周星期]+?[一1]"
    private val TUESDAY = "[周星期]+?[二2]"
    private val WEDNESDAY = "[周星期]+?[三3]"
    private val THURSDAY = "[周星期]+?[四4]"
    private val FRIDAY = "[周星期]+?[五5]"
    private val SATURDAY = "[周星期]+?[六6]"
    private val SUNDAY = "[周星期]+?[日天7]"

    internal fun getDateBefore(d: Date, day: Int): Date {
        val now = Calendar.getInstance()
        now.time = d
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day)
        return now.time
    }

    internal fun getDateAfter(d: Date, day: Int): Date {
        val now = Calendar.getInstance()
        now.time = d
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day)
        return now.time
    }

    fun weekDayConvert(i: Int): Int {
        if (i in 1..7) {
            when (i) {
                1 -> return Calendar.MONDAY
                2 -> return Calendar.TUESDAY
                3 -> return Calendar.WEDNESDAY
                4 -> return Calendar.THURSDAY
                5 -> return Calendar.FRIDAY
                6 -> return Calendar.SATURDAY
                7 -> return Calendar.SUNDAY
            }
        }
        return -1
    }

    fun weekDayTrans(context: Context, i: Int): String {
        if (i in 1..7) {
            return context.resources.getStringArray(R.array.weekdays)[i - 1]
        }
        throw IndexOutOfBoundsException("i (weekday) should >=1 && <= 7")
    }

    fun chineseToWeekDay(weekDay: String): Int {
        if (Pattern.compile(MONDAY).matcher(weekDay).find()) {
            return 1
        } else if (Pattern.compile(TUESDAY).matcher(weekDay).find()) {
            return 2
        } else if (Pattern.compile(WEDNESDAY).matcher(weekDay).find()) {
            return 3
        } else if (Pattern.compile(THURSDAY).matcher(weekDay).find()) {
            return 4
        } else if (Pattern.compile(FRIDAY).matcher(weekDay).find()) {
            return 5
        } else if (Pattern.compile(SATURDAY).matcher(weekDay).find()) {
            return 6
        } else if (Pattern.compile(SUNDAY).matcher(weekDay).find()) {
            return 7
        }
        return 1
    }
}
