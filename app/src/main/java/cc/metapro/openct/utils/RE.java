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

import android.text.TextUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RE {

    private static String timeDuringPattern = "[0-9]{1,2}";
    private static String lastNumberPattern = "[0-9]+(?=[^0-9]*$)";
    private static Pattern empty = Pattern.compile("^\\s+$");

    public static int[] getStartEnd(String s) {
        Matcher first = Pattern.compile(timeDuringPattern).matcher(s);
        if (first.find()) {
            int start = Integer.parseInt(first.group());
            Matcher last = Pattern.compile(lastNumberPattern).matcher(s);
            int end = start;
            if (last.find()) {
                end = Integer.parseInt(last.group());
            }
            return new int[]{start, end};
        } else {
            return new int[]{-1, -1};
        }
    }

    public static boolean isEmpty(String s) {
        if (TextUtils.isEmpty(s)) return true;
        Matcher m = empty.matcher(s);
        return m.find();
    }

    public static Date getDateBefore(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
        return now.getTime();
    }

    public static Date getDateAfter(Date d, int day) {
        Calendar now = Calendar.getInstance();
        now.setTime(d);
        now.set(Calendar.DATE, now.get(Calendar.DATE) + day);
        return now.getTime();
    }

}
