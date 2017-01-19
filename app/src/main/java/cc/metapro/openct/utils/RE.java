package cc.metapro.openct.utils;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RE {

    private static Pattern time_during_pattern = Pattern.compile("[0-9]{1,2}");
    private static Pattern empty = Pattern.compile("^\\s+$");


    public static int[] getStartEnd(String s) {
        int[] res = {-1, -1};
        Matcher m = time_during_pattern.matcher(s);
        for (int i = 0; i < res.length; i++) {
            if (m.find()) {
                res[i] = Integer.parseInt(m.group());
            }
        }
        if (res[0] > res[1]) {
            return new int[]{-1, -1};
        }
        return res;
    }

    public static boolean isEmpty(String s) {
        if (s == null) return true;
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
