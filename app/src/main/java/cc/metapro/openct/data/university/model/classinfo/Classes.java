package cc.metapro.openct.data.university.model.classinfo;

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

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;

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

    public boolean setInfoByName(String name, EnrichedClassInfo newInfo) {
        EnrichedClassInfo target = null;
        for (EnrichedClassInfo info : this) {
            String s = info.getName();
            if (TextUtils.isEmpty(s) && s.equalsIgnoreCase(name)) {
                target = info;
                break;
            }
        }

        if (target != null) {
            remove(target);
            return add(newInfo);
        }
        return false;
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
        if (week < 0 || week > 30) return getAllClasses();

        List<SingleClass> weekClasses = new ArrayList<>();
        List<SingleClass> notWeekClasses = new ArrayList<>();
        for (EnrichedClassInfo info : this) {
            Set<ClassTime> weekTimeSet = info.hasClassThisWeek(week);
            if (!weekTimeSet.isEmpty()) {
                for (ClassTime time : weekTimeSet) {
                    String place = time.getPlace();
                    String teacher = time.getTeacher();
                    weekClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, info.getColor()));
                }

                // 添加本周有安排但是没有完全安排的课
                Set<ClassTime> allTimeSet = info.getTimeSet();
                if (weekTimeSet.size() < allTimeSet.size()) {
                    for (ClassTime time : allTimeSet) {
                        if (!weekTimeSet.contains(time)) {
                            String place = time.getPlace();
                            String teacher = time.getTeacher();
                            notWeekClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, Color.LTGRAY));
                        }
                    }
                }
            } else {
                // 添加本周没有安排的课
                for (ClassTime time : info.getTimeSet()) {
                    String place = time.getPlace();
                    String teacher = time.getTeacher();
                    notWeekClasses.add(new SingleClass(info.getName(), info.getType(), time, place, teacher, Color.LTGRAY));
                }
            }
        }

        // ensure unique class
        List<SingleClass> result = new ArrayList<>(weekClasses);
        for (SingleClass a : notWeekClasses) {
            boolean found = false;
            for (SingleClass b : weekClasses) {
                if (a.getClassTime().equals(b.getClassTime())) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                result.add(a);
            }
        }
        return result;
    }

    @NonNull
    private List<SingleClass> getAllClasses() {
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
