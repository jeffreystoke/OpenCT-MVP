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

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.Calendar;

import cc.metapro.openct.data.university.ClassTableInfo;
import cc.metapro.openct.utils.ClassInfoHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.DateHelper;

public class ClassTime implements Comparable<ClassTime> {

    private int weekDay = 1;
    private int dailySeq = 1;
    private int dailyEnd = 1;

    private String teacher = "";
    private String place = "";

    private boolean[] weeks = new boolean[Constants.WEEKS];

    public ClassTime(ClassTime classTime) {
        place = classTime.place;
        teacher = classTime.teacher;
    }

    public ClassTime() {

    }

    ClassTime(int weekDay, int dailySeq, int dailyEnd, String teacher, String place, boolean[] weeks) {
        this.weekDay = weekDay;
        this.dailySeq = dailySeq;
        this.dailyEnd = dailyEnd;
        this.teacher = teacher;
        this.place = place;
        this.weeks = weeks;
    }

    ClassTime(int weekDay, int dailySeq, String[] contents, ClassTableInfo info) {
        this(ClassInfoHelper.infoParser(info.mTimeIndex, info.mTimeRE, contents), weekDay, dailySeq);
        teacher = ClassInfoHelper.infoParser(info.mTeacherIndex, info.mTeacherRE, contents);
        place = ClassInfoHelper.infoParser(info.mPlaceIndex, info.mPlaceRE, contents);

        String rawDuring = "";
        if (info.mDuringIndex < contents.length && info.mDuringIndex >= 0) {
            rawDuring = contents[info.mDuringIndex];
        }

        weeks = ClassInfoHelper.combineDuring(info.mDuringRE, rawDuring, weeks);
    }

    private ClassTime(String time, int weekDay, int dailySeq) {
        int length = ClassInfoHelper.getLength(time);
        if (length <= 0 || length > 5) {
            length = 1;
        }
        this.dailySeq = dailySeq;
        if (this.dailySeq <= 0) {
            this.dailySeq = 1;
        }
        this.weekDay = weekDay;
        this.dailyEnd = dailySeq + length - 1;
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

    public boolean hasClass(int week) {
        if (weeks == null) {
            weeks = new boolean[Constants.WEEKS];
        }
        return week > 0 && week <= Constants.WEEKS && weeks[week - 1];
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public int getDailySeq() {
        return dailySeq;
    }

    public void setDailySeq(int dailySeq) {
        this.dailySeq = dailySeq;
    }

    public int getDailyEnd() {
        return dailyEnd;
    }

    public void setDailyEnd(int dailyEnd) {
        this.dailyEnd = dailyEnd;
    }

    public int getLength() {
        return dailyEnd - dailySeq + 1;
    }

    public String getTimeString() {
        if (dailyEnd - dailySeq <= 0) {
            return dailySeq + "";
        } else {
            return dailySeq + " - " + dailyEnd;
        }
    }

    boolean inSameDay(ClassTime time) {
        return time != null && time.weekDay == weekDay;
    }

    boolean inSameDay(Calendar calendar) {
        return DateHelper.weekDayConvert(weekDay) == calendar.get(Calendar.DAY_OF_WEEK);
    }

    public void enableWeek(int week) {
        if (weeks == null) {
            weeks = new boolean[Constants.WEEKS];
        }
        if (week > 0 && week <= Constants.WEEKS) {
            weeks[week - 1] = true;
        }
    }

    public void disableWeek(int week) {
        if (weeks == null) {
            weeks = new boolean[Constants.WEEKS];
        }
        if (week > 0 && week <= Constants.WEEKS) {
            weeks[week - 1] = false;
        }
    }

    void combineDuring(boolean[] during) {
        if (during == null) return;
        for (int i = 0; i < during.length; i++) {
            if (during[i]) {
                this.weeks[i] = true;
            }
        }
    }

    boolean[] getDuring() {
        return weeks;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ClassTime) {
            ClassTime time = (ClassTime) obj;
            return time.weekDay == weekDay
                    && time.dailySeq == dailySeq
                    && TextUtils.isEmpty(place) && place.equals(time.place)
                    && TextUtils.isEmpty(teacher) && teacher.equals(time.teacher);
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull ClassTime o) {
        if (weekDay == o.weekDay) {
            if (dailySeq < o.dailySeq) {
                return -1;
            } else {
                return 1;
            }
        } else if (weekDay < o.weekDay) {
            return -1;
        } else if (weekDay > o.weekDay) {
            return 1;
        }

        return 0;
    }

}
