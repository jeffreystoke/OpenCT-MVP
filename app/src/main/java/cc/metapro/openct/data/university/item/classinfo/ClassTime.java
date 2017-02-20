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
import android.support.v4.util.ArraySet;

import java.util.Set;

import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.ClassInfoHelper;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.REHelper;

public class ClassTime implements Comparable<ClassTime> {

    private int weekDay;
    private int dailySeq;
    private int length;

    private String teacher;
    private String place;
    private Set<ClassDuring> mDuringSet = new ArraySet<>();

    ClassTime(int weekDay, int dailySeq, String[] contents, CmsFactory.ClassTableInfo info) {
        this(ClassInfoHelper.infoParser(info.mTimeIndex, info.mTimeRE, contents), weekDay, dailySeq);
        teacher = ClassInfoHelper.infoParser(info.mTeacherIndex, info.mTeacherRE, contents);
        place = ClassInfoHelper.infoParser(info.mPlaceIndex, info.mPlaceRE, contents);
        String rawDuring = "";
        if (info.mDuringIndex < contents.length && info.mDuringIndex >= 0) {
            rawDuring = contents[info.mDuringIndex];
        }
        mDuringSet.add(ClassInfoHelper.getClassDuring(info.mDuringRE, rawDuring));
    }

    private ClassTime(String time, int weekDay, int dailySeq) {
        length = ClassInfoHelper.getLength(time);
        if (length <= 0 || length > 5) {
            length = 1;
        }
        this.dailySeq = dailySeq;
        if (this.dailySeq <= 0) {
            this.dailySeq = 1;
        }
        this.weekDay = weekDay;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Set<ClassDuring> getDuringSet() {
        return mDuringSet;
    }

    public void addDuring(ClassDuring during) {
        mDuringSet.add(during);
    }

    public void removeDuring(ClassDuring during) {
        mDuringSet.remove(during);
    }

    public boolean isEmpty() {
        return mDuringSet == null || mDuringSet.isEmpty();
    }

    public int getWeekDay() {
        return weekDay;
    }

    public int getDailySeq() {
        return dailySeq;
    }

    public int getLength() {
        return length;
    }

    public String getTime() {
        if (length == 1) {
            return dailySeq + "";
        } else {
            return dailySeq + " - " + (dailySeq + length - 1);
        }
    }

    public boolean inSameDay(ClassTime time) {
        return time != null && time.weekDay == weekDay;
    }

    public boolean inSameDay(int weekDay) {
        return DateHelper.weekDayTrans(this.weekDay) == weekDay;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ClassTime) {
            ClassTime time = (ClassTime) obj;
            return time.weekDay == weekDay && time.dailySeq == dailySeq && time.length == length;
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(@NonNull ClassTime o) {
        if (weekDay == o.weekDay) {
            if (dailySeq < o.dailySeq) {
                return -1;
            } else {
                return 1;
            }
        }
        return 0;
    }
}
