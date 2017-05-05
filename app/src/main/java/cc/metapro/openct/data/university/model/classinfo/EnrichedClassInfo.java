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
import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.Set;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.data.university.ClassTableInfo;
import cc.metapro.openct.utils.CharacterParser;
import cc.metapro.openct.utils.ClassInfoHelper;

/**
 * One EnrichedClassInfo Object has one ClassInfo Object
 */
public class EnrichedClassInfo implements Comparable<EnrichedClassInfo> {

    private String name;

    private String type;

    private Set<ClassTime> mTimeSet = new ArraySet<>();

    private int color = Color.parseColor("#968bc34a");

    public EnrichedClassInfo(String name, String type, ClassTime time) {
        this.name = name;
        this.type = type;
        if (time != null) {
            mTimeSet.add(time);
        }
    }

    /**
     * only one class info content should be there
     *
     * @param content string class info content
     * @param weekday day of week
     * @param info    cms class table for class info generation
     * @param color   color of background, same classes share the same color
     */
    public EnrichedClassInfo(String content, int weekday, int dailySeq, int color, ClassTableInfo info) {
        this(content, weekday, dailySeq, info);
        this.color = color;
    }

    public EnrichedClassInfo(String content, int weekday, int dailySeq, ClassTableInfo info) {
        String[] infoContents = content.split(HTMLUtils.BR_REPLACER);
        name = ClassInfoHelper.infoParser(info.mNameIndex, info.mNameRE, infoContents);
        type = ClassInfoHelper.infoParser(info.mTypeIndex, info.mTypeRE, infoContents);
        mTimeSet.add(new ClassTime(weekday, dailySeq, infoContents, info));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * judge whether this class is on schedule in this week
     *
     * @param week current week or selected week
     * @return time when has class this week
     */
    @NonNull
    Set<ClassTime> hasClassThisWeek(int week) {
        Set<ClassTime> result = new ArraySet<>();
        for (ClassTime time : mTimeSet) {
            if (time.hasClass(week)) {
                result.add(time);
            }
        }
        return result;
    }

    /**
     * judge whether this class is on schedule today
     *
     * @param week current week or selected week
     * @return time when has class today
     */
    @NonNull
    Set<ClassTime> hasClassToday(int week) {
        Calendar calendar = Calendar.getInstance();
        Set<ClassTime> timeList = hasClassThisWeek(week);
        Set<ClassTime> result = new ArraySet<>();
        for (ClassTime time : timeList) {
            if (time.inSameDay(calendar)) {
                result.add(time);
            }
        }
        return result;
    }

    /**
     * merge class with same name to one, the differences should be in time, place, teacher, during
     * and time is the key of them
     *
     * @param info another class info which has the same name with this one
     */
    void combine(EnrichedClassInfo info) {
        if (info == null) return;
        ArraySet<ClassTime> mixedTime = new ArraySet<>();
        mixedTime.addAll(mTimeSet);

        for (ClassTime time : info.getTimeSet()) {
            boolean found = false;
            for (ClassTime myTime : mTimeSet) {
                if (myTime.equals(time)) {
                    found = true;
                    myTime.combineDuring(time.getDuring());
                    mixedTime.add(myTime);
                }
            }
            if (!found) {
                mixedTime.add(time);
            }
        }
        mTimeSet = mixedTime;
    }

    /**
     * get a set of class time, which includes daily time, during, place and teacher
     */
    public Set<ClassTime> getTimeSet() {
        return mTimeSet;
    }

    public void setTimes(Set<ClassTime> timeSet) {
        if (timeSet != null && !timeSet.isEmpty()) {
            mTimeSet = timeSet;
        }
    }

    public boolean isEmpty() {
        return mTimeSet == null || mTimeSet.isEmpty();
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : StoreHelper.toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof EnrichedClassInfo) {
            EnrichedClassInfo classInfo = (EnrichedClassInfo) obj;
            return !TextUtils.isEmpty(name) && name.equals(classInfo.name);
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull EnrichedClassInfo o) {
        CharacterParser parser = CharacterParser.getInstance();
        String me = parser.getSpelling(name);
        String that = parser.getSpelling(o.name);
        return me.compareTo(that);
    }

}
