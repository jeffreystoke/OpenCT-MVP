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

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Keep
public final class REHelper {

    private static final String timeDuringPattern = "([0-2]?[0-9]?)";
    private static final String lastNumberPattern = "([0-2]?[0-9]?)(?=[^0-9]*$)";
    private static final String emptyPattern = "^\\s+$";
    private static final String oddPatternString = "(单周?)";
    private static final String evenPatternString = "(双周?)";
    private static final String doubleWidthString = "[^\\x00-\\xff]";

//    public static List<int[]> getAllStartEnd(@Nullable String s) {
//        if (TextUtils.isEmpty(s)) {
//            return new ArrayList<>();
//        }
//        List<int[]> result = new ArrayList<>();
//        String[] duringStrings = s.split(ClassInfo.DURING_SEP);
//        for (String during : duringStrings) {
//            result.add(getStartEnd(during));
//        }
//        return result;
//    }

    /**
     * 获取一节课的开始和结束时间, 课程周期, 范围 0 - 29
     */
    @NonNull
    public static int[] getStartEnd(@Nullable String s) {
        if (TextUtils.isEmpty(s)) {
            return new int[]{-1, -1};
        }
        Matcher first = Pattern.compile(timeDuringPattern).matcher(s);
        if (first.find()) {
            String sS = first.group();
            if (!TextUtils.isEmpty(sS)) {
                int start = Integer.parseInt(first.group());
                Matcher last = Pattern.compile(lastNumberPattern).matcher(s);
                int end = start;
                if (last.find()) {
                    String eS = last.group();
                    if (!TextUtils.isEmpty(eS)) {
                        end = Integer.parseInt(eS);
                    }
                }
                return new int[]{start, end};
            } else {
                return new int[]{-1, -1};
            }
        } else {
            return new int[]{-1, -1};
        }
    }

    public static boolean isOddWeek(String s) {
        return !TextUtils.isEmpty(s) && Pattern.compile(oddPatternString).matcher(s).find();
    }

    public static boolean isEvenWeek(String s) {
        return !TextUtils.isEmpty(s) && Pattern.compile(evenPatternString).matcher(s).find();
    }

    public static String delDoubleWidthChar(String s) {
        return TextUtils.isEmpty(s) ? "" : s.replaceAll(doubleWidthString, "");
    }

    public static boolean isEmpty(String s) {
        return TextUtils.isEmpty(s) || Pattern.compile(emptyPattern).matcher(s).find();
    }

}
