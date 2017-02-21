package cc.metapro.openct.utils;

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

import android.support.annotation.NonNull;
import android.util.SparseArray;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.UidGenerator;

import java.net.SocketException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.data.university.item.classinfo.ClassTime;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;

public class ICalHelper {

    @NonNull
    public static List<VEvent> getClassEvents(SparseArray<Calendar> classTimeMap, int week, EnrichedClassInfo info) throws URISyntaxException, SocketException {
        List<VEvent> result = new ArrayList<>();
        for (ClassTime time : info.getTimeSet()) {
            for (int i = 1; i <= 30; i++) {
                int j = i;
                while (time.hasClass(j++)) ;
                j--;
                VEvent event = getClassEvent(classTimeMap, info, time, i, j, week);
                if (event != null) {
                    result.add(event);
                }
            }
        }

        return result;
    }

    private static VEvent getClassEvent(SparseArray<Calendar> classTimeMap, EnrichedClassInfo info, ClassTime time, int startWeek, int endWeek, int currentWeek) throws URISyntaxException, SocketException {
        Calendar now = Calendar.getInstance();

        int dayAfter = (now.get(Calendar.WEEK_OF_YEAR) + endWeek - currentWeek - 1) * 7;
        int dayBefore = Math.abs((now.get(Calendar.WEEK_OF_YEAR) + startWeek - currentWeek - 1) * 7);

        // repeat every week until endDate
        Recur recur = new Recur(Recur.WEEKLY, new DateTime(DateHelper.getDateAfter(now.getTime(), dayAfter)));
        recur.setInterval(1);
        RRule rule = new RRule(recur);

        Calendar startTime = classTimeMap.get(time.getDailySeq());
        Calendar endTime = classTimeMap.get(time.getDailySeq() + time.getLength());

        Calendar dailyStart = Calendar.getInstance();
        dailyStart.setTime(DateHelper.getDateBefore(now.getTime(), dayBefore));
        dailyStart.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
        dailyStart.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
        dailyStart.set(Calendar.DAY_OF_WEEK, DateHelper.weekDayTrans(time.getWeekDay()));
        DateTime start = new DateTime(dailyStart.getTime());

        Calendar dailyEnd = Calendar.getInstance();
        dailyEnd.setTime(DateHelper.getDateBefore(now.getTime(), dayBefore));
        dailyEnd.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
        dailyEnd.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
        dailyEnd.set(Calendar.DAY_OF_WEEK, DateHelper.weekDayTrans(time.getWeekDay()));
        DateTime end = new DateTime(dailyEnd.getTime());

        ParameterList paraList = new ParameterList();
        paraList.add(ParameterFactoryImpl.getInstance().createParameter
                (Value.PERIOD.getName(), Value.PERIOD.getValue()));

        PeriodList periodList = new PeriodList();
        periodList.add(new Period(start, end));
        RDate rdate = new RDate(paraList, periodList);

        // create event, repeat weekly
        VEvent event = new VEvent(start, end, info.getName());

        // set event
        event.getProperties().add(new Uid(new UidGenerator("OPENCT").generateUid().getValue()));
        event.getProperties().add(new Location(time.getPlace()));

        event.getProperties().add(new Description(time.getTime() + " 节"));
        event.getProperties().add(rdate);
        event.getProperties().add(rule);
        return event;
    }

}
