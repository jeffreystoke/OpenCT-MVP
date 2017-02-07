package cc.metapro.openct.data.university.item;

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

import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.ParameterFactoryImpl;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.RDate;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.util.UidGenerator;

import java.util.Calendar;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.REHelper;

@Keep
public class ClassInfo {

    private String mUid;
    private String mName;
    private String mType;
    private String mTime;
    private String mDuring;
    private String mTeacher;
    private String mPlace;
    private boolean mOddWeek;
    private boolean mEvenWeek;
    private ClassInfo mSubClassInfo;

    public ClassInfo() {
        mUid = UUID.randomUUID().toString();
    }

    public ClassInfo(String uid, String name, String type, String time, String during, String teacher, String place, boolean oddWeek, boolean evenWeek) {
        if (TextUtils.isEmpty(uid)) {
            mUid = UUID.randomUUID().toString();
        } else {
            mUid = uid;
        }
        mName = name;
        mType = type;
        mTime = time;
        mDuring = during;
        mTeacher = teacher;
        mPlace = place;
        mOddWeek = oddWeek;
        mEvenWeek = evenWeek;
    }

    public ClassInfo(String content, CmsFactory.ClassTableInfo info) {
        mUid = UUID.randomUUID().toString();
        String[] classes = content.split(HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+");
        String s = classes[0];
        String[] tmp = s.split(HTMLUtils.BR_REPLACER);

        if (tmp.length > 0) {
            int duringIndex = info.mDuringIndex;
            int placeIndex = info.mPlaceIndex;
            int timeIndex = info.mTimeIndex;
            int teacherIndex = info.mTeacherIndex;
            if (tmp.length == info.mClassStringCount - 1) {
                duringIndex--;
                placeIndex--;
                timeIndex--;
                teacherIndex--;
            }
            mName = infoParser(info.mNameIndex, info.mNameRE, tmp);
            mType = infoParser(info.mTypeIndex, info.mTypeRE, tmp);
            mTeacher = infoParser(teacherIndex, info.mTeacherRE, tmp);
            mPlace = infoParser(placeIndex, info.mPlaceRE, tmp);

            if (timeIndex < tmp.length && timeIndex >= 0) {
                mOddWeek = REHelper.isOddWeek(tmp[timeIndex]);
                mEvenWeek = REHelper.isEvenWeek(tmp[timeIndex]);
            }

            mTime = infoParser(timeIndex, info.mTimeRE, tmp);
            if (!REHelper.isEmpty(mTime)) {
                mTime = REHelper.delDoubleWidthChar(mTime);
            }
            mDuring = infoParser(duringIndex, info.mDuringRE, tmp);
            if (REHelper.isEmpty(mDuring)) {
                mDuring = REHelper.delDoubleWidthChar(mDuring);
            }
        }

        // create all subclass
        if (classes.length > 1) {
            String subContent = "";
            for (int i = 1; i < classes.length; i++) {
                if (i < classes.length - 1) {
                    subContent += classes[i] + HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER;
                } else {
                    subContent += classes[i];
                }
            }
            mSubClassInfo = new ClassInfo(subContent, info);
        }
    }

    private String infoParser(int idx, String re, String[] contents) {
        if (idx < contents.length) {
            String content = contents[idx];
            if (!TextUtils.isEmpty(re)) {
                Pattern pattern = Pattern.compile(re);
                Matcher m = pattern.matcher(content);
                if (m.find())
                    content = m.group();
            }
            return content;
        } else {
            return "";
        }
    }

    public String getId() {
        return mUid;
    }

    public boolean hasClass(int week) {
        if (TextUtils.isEmpty(mDuring)) return false;
        int[] startEnd = REHelper.getStartEnd(mDuring);
        if (week >= startEnd[0] && week <= startEnd[1]) {
            if (mOddWeek && (week % 2 == 1)) return true;
            if (mEvenWeek && (week % 2 == 0)) return true;
            if (!mEvenWeek && !mOddWeek) return true;
        }
        return false;
    }

    public int getLength() {
        if (TextUtils.isEmpty(mTime)) return 1;
        int[] startEnd = REHelper.getStartEnd(mTime);
        try {
            if (startEnd[0] == -1) {
                return Integer.parseInt(mTime);
            }
        } catch (Exception e) {
            return 1;
        }
        return startEnd[1] - startEnd[0] + 1;
    }

    @Nullable
    public String getDuring() {
        return TextUtils.isEmpty(mDuring) ? null : mDuring;
    }

    @Nullable
    public String getTime() {
        return TextUtils.isEmpty(mTime) ? null : mTime;
    }

    public boolean isEmpty() {
        return REHelper.isEmpty(mName);
    }

    boolean hasSubClass() {
        return mSubClassInfo != null;
    }

    public ClassInfo getSubClassInfo() {
        return mSubClassInfo;
    }

    public void setSubClassInfo(ClassInfo info) {
        mSubClassInfo = info;
    }

    public String getName() {
        return TextUtils.isEmpty(mName) ? "" : mName;
    }

    public String getTeacher() {
        return mTeacher;
    }

    public String getType() {
        return mType;
    }

    public String getPlace() {
        return TextUtils.isEmpty(mPlace) ? "" : mPlace;
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClassInfo) {
            ClassInfo c = (ClassInfo) obj;
            if (TextUtils.isEmpty(c.mUid) || TextUtils.isEmpty(mUid)) {
                if (c.getName().equals(mName)) {
                    return true;
                }
            } else {
                if (c.mUid.equals(mUid)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isOddWeek() {
        return mOddWeek;
    }

    public boolean isEvenWeek() {
        return mEvenWeek;
    }

    /**
     * 根据课表信息, 设定的当前周以及当前时间 生成iCal日历事件
     *
     * @param week    第几周
     * @param weekDay 星期几
     * @return iCal日历事件
     */
    @Nullable
    public VEvent getEvent(int week, int weekDay) {
        try {
            // set end Date
            Calendar now = Calendar.getInstance();
            int[] startEnd = REHelper.getStartEnd(mDuring);

            int dayAfter = (now.get(Calendar.WEEK_OF_YEAR) + startEnd[1] - week - 1) * 7;

            // repeat every week until endDate
            Recur recur = new Recur(Recur.WEEKLY,
                    new DateTime(DateHelper.getDateAfter(now.getTime(), dayAfter)));
            recur.setInterval(1);
            RRule rule = new RRule(recur);

            // set event period
            int dayBefore = Math.abs(
                    (now.get(Calendar.WEEK_OF_YEAR) + startEnd[0] - week - 1) * 7);

            Calendar dailyStart = Calendar.getInstance();
            dailyStart.setTime(DateHelper.getDateBefore(now.getTime(), dayBefore));
            dailyStart.set(Calendar.HOUR_OF_DAY, 8);
            dailyStart.set(Calendar.MINUTE, 0);
            dailyStart.set(Calendar.DAY_OF_WEEK, weekDay);
            DateTime start = new DateTime(dailyStart.getTime());

            Calendar dailyEnd = Calendar.getInstance();
            dailyEnd.setTime(DateHelper.getDateBefore(now.getTime(), dayBefore));
            dailyEnd.set(Calendar.HOUR_OF_DAY, 17);
            dailyEnd.set(Calendar.MINUTE, 0);
            dailyEnd.set(Calendar.DAY_OF_WEEK, weekDay);
            DateTime end = new DateTime(dailyEnd.getTime());

            ParameterList paraList = new ParameterList();
            paraList.add(ParameterFactoryImpl.getInstance().createParameter
                    (Value.PERIOD.getName(), Value.PERIOD.getValue()));

            PeriodList periodList = new PeriodList();
            periodList.add(new Period(start, end));
            RDate rdate = new RDate(paraList, periodList);

            // create event, repeat weekly
            VEvent event = new VEvent(start, end, mName + "@" + mPlace + ", " + mTime);

            // set mUid
            event.getProperties().add(new Uid(new UidGenerator("OPENCT").generateUid().getValue()));

            // set event
            event.getProperties().add(rdate);
            event.getProperties().add(rule);
            return event;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
