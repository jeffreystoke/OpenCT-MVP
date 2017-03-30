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


import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public final class DateHelper {

    private static final String MONDAY = "[周星期]+?[一1]";
    private static final String TUESDAY = "[周星期]+?[二2]";
    private static final String WEDNESDAY = "[周星期]+?[三3]";
    private static final String THURSDAY = "[周星期]+?[四4]";
    private static final String FRIDAY = "[周星期]+?[五5]";
    private static final String SATURDAY = "[周星期]+?[六6]";
    private static final String SUNDAY = "[周星期]+?[日天7]";

    static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    static Date getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

    public static int weekDayTrans(int i) {
        if (i > 0 && i < 8) {
            switch (i) {
                case 1:
                    return Calendar.MONDAY;
                case 2:
                    return Calendar.TUESDAY;
                case 3:
                    return Calendar.WEDNESDAY;
                case 4:
                    return Calendar.THURSDAY;
                case 5:
                    return Calendar.FRIDAY;
                case 6:
                    return Calendar.SATURDAY;
                case 7:
                    return Calendar.SUNDAY;
            }
        }
        return -1;
    }

    public static String weekDayToChinese(int i) {
        switch (i) {
            case 1:
                return "周一";
            case 2:
                return "周二";
            case 3:
                return "周三";
            case 4:
                return "周四";
            case 5:
                return "周五";
            case 6:
                return "周六";
            case 7:
                return "周日";
            default:
                return "";
        }
    }

    public static int chineseToWeekDay(String weekDay) {
        if (Pattern.compile(MONDAY).matcher(weekDay).find()) {
            return 1;
        } else if (Pattern.compile(TUESDAY).matcher(weekDay).find()) {
            return 2;
        } else if (Pattern.compile(WEDNESDAY).matcher(weekDay).find()) {
            return 3;
        } else if (Pattern.compile(THURSDAY).matcher(weekDay).find()) {
            return 4;
        } else if (Pattern.compile(FRIDAY).matcher(weekDay).find()) {
            return 5;
        } else if (Pattern.compile(SATURDAY).matcher(weekDay).find()) {
            return 6;
        } else if (Pattern.compile(SUNDAY).matcher(weekDay).find()) {
            return 7;
        }
        return 1;
    }
}
