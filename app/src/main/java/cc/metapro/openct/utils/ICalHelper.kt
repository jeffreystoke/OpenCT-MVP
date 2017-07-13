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

import android.util.SparseArray
import cc.metapro.openct.data.university.model.classinfo.ClassTime
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.parameter.Value
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.util.UidGenerator
import java.net.SocketException
import java.net.URISyntaxException
import java.util.*
import java.util.Calendar

object ICalHelper {

    @Throws(URISyntaxException::class, SocketException::class)
    fun getClassEvents(classTimeMap: SparseArray<Calendar>, week: Int, everyClassTime: Int, restTime: Int, info: EnrichedClassInfo): List<VEvent> {
        val result = ArrayList<VEvent>()
        for (time in info.timeSet) {
            var i = 1
            while (i <= 30) {
                if (time.hasClass(i)) {
                    var j = i
                    while (time.hasClass(++j))
                        j--
                    val event = getClassEvent(classTimeMap, info, time, i, j, week, everyClassTime, restTime)
                    if (event != null) {
                        event.validate()
                        result.add(event)
                    }
                    i += j - i
                }
                i++
            }
        }
        return result
    }

    /**
     * @param classTimeMap contains daily class time info user had set
     * @param info         to generate event
     * @param time         a piece of time in info
     * @param startWeek    which week the event happen
     * @param endWeek      which week the event end
     * @param currentWeek  current week of this semi
     * @return a fully configured event
     */
    @Throws(URISyntaxException::class, SocketException::class)
    private fun getClassEvent(classTimeMap: SparseArray<Calendar>,
                              info: EnrichedClassInfo,
                              time: ClassTime,
                              startWeek: Int,
                              endWeek: Int,
                              currentWeek: Int,
                              everyClassTime: Int,
                              restTime: Int
    ): VEvent? {
        val now = Calendar.getInstance(Locale.CHINA)
        val dayBefore = (currentWeek - startWeek) * 7
        val dayAfter = (endWeek - currentWeek) * 7

        // repeat every week until endDate
        val recur = Recur(Recur.WEEKLY, DateTime(DateHelper.getDateAfter(now.time, dayAfter)))
        recur.interval = 1
        val rule = RRule(recur)

        val startTime = classTimeMap.get(time.dailySeq)
        val length = time.length
        val endTime = GregorianCalendar(
                startTime.get(Calendar.YEAR),
                startTime.get(Calendar.MONTH),
                startTime.get(Calendar.DAY_OF_MONTH),
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE) + length * everyClassTime + restTime * (length - 1))

        val dailyStart = Calendar.getInstance()
        dailyStart.time = DateHelper.getDateBefore(now.time, dayBefore)
        dailyStart.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY))
        dailyStart.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE))
        dailyStart.set(Calendar.DAY_OF_WEEK, DateHelper.weekDayConvert(time.weekDay))
        val start = DateTime(dailyStart.time)

        val dailyEnd = Calendar.getInstance()
        dailyEnd.time = DateHelper.getDateBefore(now.time, dayBefore)
        dailyEnd.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY))
        dailyEnd.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE))
        dailyEnd.set(Calendar.DAY_OF_WEEK, DateHelper.weekDayConvert(time.weekDay))
        val end = DateTime(dailyEnd.time)

        val paraList = ParameterList()
        paraList.add(ParameterFactoryImpl.getInstance().createParameter(Value.PERIOD.name, Value.PERIOD.value))

        val periodList = PeriodList()
        periodList.add(Period(start, end))
        val rdate = RDate(paraList, periodList)

        // create event, repeat weekly
        val event = VEvent(start, end, info.name)

        // set event
        event.properties.add(Uid(UidGenerator("OPENCT").generateUid().value))
        event.properties.add(Location(time.place + " " + time.teacher))

        event.properties.add(Description(time.timeString + " " + time.teacher))
        event.properties.add(rdate)
        event.properties.add(rule)
        return event
    }

}
