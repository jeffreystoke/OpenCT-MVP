package cc.metapro.openct.data.university.item.classinfo;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Classes extends ArrayList<EnrichedClassInfo> {

    @Override
    public boolean add(EnrichedClassInfo classInfo) {
        EnrichedClassInfo targetInfo = null;
        for (EnrichedClassInfo c : this) {
            if (c.equals(classInfo)) {
                targetInfo = c;
                break;
            }
        }
        if (targetInfo != null) {
            remove(targetInfo);
            targetInfo.combine(classInfo);
            super.add(targetInfo);
        } else {
            super.add(classInfo);
        }
        return super.add(classInfo);
    }

    @Override
    public void add(int index, EnrichedClassInfo element) {
        add(element);
    }

    @NonNull
    public List<SingleClass> getTodayClasses(int week) {
        List<SingleClass> todayClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            List<ClassTime> timeList = info.hasClassToday(week);
            if (!timeList.isEmpty()) {
                for (ClassTime time : timeList) {
                    ClassInfo classInfo = info.getTimeMap().get(time);
                    String place = classInfo.getPlace();
                    String teacher = classInfo.getTeacher();
                    todayClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher));
                }
            }
        }
        Collections.sort(todayClasses);
        return todayClasses;
    }

    @NonNull
    public List<SingleClass> getWeekClasses(int week) {
        List<SingleClass> todayClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            List<ClassTime> timeList = info.hasClassThisWeek(week);
            if (!timeList.isEmpty()) {
                for (ClassTime time : timeList) {
                    ClassInfo classInfo = info.getTimeMap().get(time);
                    String place = classInfo.getPlace();
                    String teacher = classInfo.getTeacher();
                    todayClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher));
                }
            }
        }
        Collections.sort(todayClasses);
        return todayClasses;
    }
}
