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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassInfoHelper {

    public static int getLength(String time) {
        int[] tmp = REHelper.getStartEnd(time);
        return tmp[1] - tmp[0];
    }

    public static String infoParser(int idx, String re, String[] contents) {
        if (idx < contents.length && idx >= 0) {
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

    public static boolean[] getDuring(String during) {
        boolean[] weeks = new boolean[30];

        return weeks;
    }

    public static boolean isEven(String during) {
        return Pattern.compile("双周?").matcher(during).find();
    }

    public static boolean isOdd(String during) {
        return Pattern.compile("单周?").matcher(during).find();
    }
}
