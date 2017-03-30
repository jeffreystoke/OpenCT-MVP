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
import java.util.Set;

import cc.metapro.openct.utils.REHelper;


public class Classes extends ArrayList<EnrichedClassInfo> {

    @Override
    public boolean add(EnrichedClassInfo classInfo) {
        if (REHelper.isEmpty(classInfo.getName()))
            return false;
        EnrichedClassInfo targetInfo = null;

        for (EnrichedClassInfo c : this) {
            if (c.equals(classInfo)) {
                targetInfo = c;
                break;
            }
        }

        if (targetInfo != null) {
            super.remove(targetInfo);
            targetInfo.combine(classInfo);
            super.add(0, targetInfo);
        } else {
            super.add(0, classInfo);
        }
        return true;
    }

    @Override
    public void add(int index, EnrichedClassInfo element) {
        add(element);
    }

    @NonNull
    public List<SingleClass> getTodayClasses(int week) {
        List<SingleClass> todayClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            Set<ClassTime> timeList = info.hasClassToday(week);
            if (!timeList.isEmpty()) {
                for (ClassTime time : timeList) {
                    String place = time.getPlace();
                    String teacher = time.getTeacher();
                    todayClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, info.getColor()));
                }
            }
        }
        Collections.sort(todayClasses);
        return todayClasses;
    }

    @NonNull
    public List<SingleClass> getWeekClasses(int week) {
        List<SingleClass> weekClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            Set<ClassTime> timeList = info.hasClassThisWeek(week);
            if (!timeList.isEmpty()) {
                for (ClassTime time : timeList) {
                    String place = time.getPlace();
                    String teacher = time.getTeacher();
                    weekClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, info.getColor()));
                }
            }
        }
        return weekClasses;
    }

    @NonNull
    public List<SingleClass> getAllClasses() {
        List<SingleClass> todayClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            Set<ClassTime> timeList = info.getTimeSet();
            if (!timeList.isEmpty()) {
                for (ClassTime time : timeList) {
                    String place = time.getPlace();
                    String teacher = time.getTeacher();
                    todayClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, info.getColor()));
                }
            }
        }
        return todayClasses;
    }
}
