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

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;

import com.google.common.base.Strings;

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

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.homepage.ClassContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RE;

public class ClassInfo implements Serializable {

    private final static String oddPatternString = "(单周?)";
    private final static String evenPatternString = "(双周?)";
    private final static String doubleWidthString = "[^\\x00-\\xff]";
    private String mName;
    private String mType;
    private String mTime;
    private String mDuring;
    private String mTeacher;
    private String mPlace;
    private boolean mInactive;
    private boolean mOddWeek;
    private boolean mEvenWeek;
    private ClassInfo mSubClassInfo;

    public ClassInfo(String name, String type, String time, String during, String teacher, String place, boolean oddWeek, boolean evenWeek) {
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
        String[] classes = content.split(Constants.BR_REPLACER + Constants.BR_REPLACER + "+");
        String s = classes[0];
        String[] tmp = s.split(Constants.BR_REPLACER);
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
            if (info.mNameIndex < tmp.length) {
                mName = infoParser(info.mNameRE, tmp[info.mNameIndex]);
            }
            if (info.mTypeIndex < tmp.length) {
                mType = infoParser(info.mTypeRE, tmp[info.mTypeIndex]);
            }
            if (teacherIndex < tmp.length) {
                mTeacher = infoParser(info.mTeacherRE, tmp[teacherIndex]);
            }
            if (placeIndex < tmp.length) {
                mPlace = infoParser(info.mPlaceRE, tmp[placeIndex]);
            }
            if (timeIndex < tmp.length) {
                mTime = infoParser(info.mTimeRE, tmp[timeIndex]);
                if (!RE.isEmpty(mTime)) {
                    mTime = mTime.replaceAll(doubleWidthString, "");
                    mOddWeek = Pattern.compile(oddPatternString).matcher(tmp[timeIndex]).find();
                    mEvenWeek = Pattern.compile(evenPatternString).matcher(tmp[timeIndex]).find();
                }
            }
            if (duringIndex < tmp.length) {
                mDuring = infoParser(info.mDuringRE, tmp[duringIndex]);
                if (RE.isEmpty(mDuring)) {
                    mDuring = mDuring.replaceAll(doubleWidthString, "");
                }
            }
        }

        // create all subclass
        if (classes.length > 1) {
            String subContent = "";
            for (int i = 1; i < classes.length; i++) {
                if (i < classes.length - 1) {
                    subContent += classes[i] + Constants.BR_REPLACER + Constants.BR_REPLACER;
                } else {
                    subContent += classes[i];
                }
            }
            mSubClassInfo = new ClassInfo(subContent, info);
        }
    }

    private String infoParser(String re, String content) {
        if (!Strings.isNullOrEmpty(re)) {
            Pattern pattern = Pattern.compile(re);
            Matcher m = pattern.matcher(content);
            if (m.find()) content = m.group();
        }
        return content;
    }

    public boolean hasClass(int week) {
        if (mInactive) return false;
        if (Strings.isNullOrEmpty(mDuring)) return false;
        int[] startEnd = RE.getStartEnd(mDuring);
        if (week >= startEnd[0] && week <= startEnd[1]) {
            if (mOddWeek && (week % 2 == 1)) return true;
            if (mEvenWeek && (week % 2 == 0)) return true;
            if (!mEvenWeek && !mOddWeek) return true;
        }
        return false;
    }

    public int getLength() {
        if (Strings.isNullOrEmpty(mTime)) return 1;
        int[] startEnd = RE.getStartEnd(mTime);
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
        return Strings.isNullOrEmpty(mDuring) ? null : mDuring;
    }

    @Nullable
    public String getTime() {
        return Strings.isNullOrEmpty(mTime) ? null : mTime;
    }

    public boolean isEmpty() {
        return RE.isEmpty(mName);
    }

    public boolean hasSubClass() {
        return mSubClassInfo != null;
    }

    public ClassInfo getSubClassInfo() {
        return mSubClassInfo;
    }

    void setSubClassInfo(ClassInfo info) {
        mSubClassInfo = info;
    }

    public String getName() {
        return Strings.isNullOrEmpty(mName) ? "" : mName;
    }

    public String getTeacher() {
        return mTeacher;
    }

    public String getType() {
        return mType;
    }

    public String getPlace() {
        return Strings.isNullOrEmpty(mPlace) ? "" : mPlace;
    }

    private String toFullString() {
        StringBuilder sb = new StringBuilder();

        if (!RE.isEmpty(mName)) sb.append("课程名称: ").append(mName).append("\n\n");
        if (!RE.isEmpty(mType)) sb.append("课程类型: ").append(mType).append("\n\n");

        String time = getTime();
        if (!RE.isEmpty(time)) sb.append("上课时间: ").append(time).append("\n\n");

        if (!RE.isEmpty(mPlace)) sb.append("上课地点: ").append(mPlace).append("\n\n");
        if (!RE.isEmpty(mTeacher)) sb.append("授课教师: ").append(mTeacher).append("\n\n");

        String during = getDuring();
        if (!RE.isEmpty(during)) sb.append("课程周期: ").append(during).append("\n\n");

        if (hasSubClass()) sb.append("\n\n").append(mSubClassInfo.toFullString());

        if (sb.length() > 2 && sb.charAt(sb.length() - 1) == '\n') {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClassInfo) {
            ClassInfo c = (ClassInfo) obj;
            if (c.getName().equals(mName)) {
                return true;
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
        if (isEmpty() || mInactive)
            return null;
        try {
            // set end Date
            Calendar now = Calendar.getInstance();
            int[] startEnd = RE.getStartEnd(mDuring);

            int dayAfter = (now.get(Calendar.WEEK_OF_YEAR) + startEnd[1] - week - 1) * 7;

            // repeat every week until endDate
            Recur recur = new Recur(Recur.WEEKLY,
                    new DateTime(RE.getDateAfter(now.getTime(), dayAfter)));
            recur.setInterval(1);
            RRule rule = new RRule(recur);

            // set event period
            int dayBefore = Math.abs(
                    (now.get(Calendar.WEEK_OF_YEAR) + startEnd[0] - week - 1) * 7);

            Calendar dailyStart = Calendar.getInstance();
            dailyStart.setTime(RE.getDateBefore(now.getTime(), dayBefore));
            dailyStart.set(Calendar.HOUR_OF_DAY, 8);
            dailyStart.set(Calendar.MINUTE, 0);
            dailyStart.set(Calendar.DAY_OF_WEEK, weekDay);
            DateTime start = new DateTime(dailyStart.getTime());

            Calendar dailyEnd = Calendar.getInstance();
            dailyEnd.setTime(RE.getDateBefore(now.getTime(), dayBefore));
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

            // set uid
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

    AlertDialog.Builder getAlertDialog(final Context mContext, final ClassContract.Presenter mPresenter) {
        AlertDialog.Builder a = new AlertDialog.Builder(mContext);
        a.setMessage(toFullString());
        a.setCancelable(true);
        a.setPositiveButton("返回", null);
        a.setNeutralButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                AlertDialog.Builder b = new AlertDialog.Builder(mContext);
                b.setNegativeButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mInactive = true;
                        mPresenter.storeClasses();
                        mPresenter.loadLocalClasses();
                    }
                });
                b.setPositiveButton("取消", null);
                b.setTitle("警告");
                b.setMessage("这节课将被删除!\n\nPS: 该操作仅对今日课表和本周课表有效");
                b.show();
            }
        });
        a.setTitle("课程信息");
        return a;
    }

}
