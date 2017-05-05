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

import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassInfoHelper {

    public static int getLength(String time) {
        int[] tmp = REHelper.getStartEnd(time);
        return tmp[1] - tmp[0] + 1;
    }

    public static String infoParser(int idx, String re, String[] contents) {
        if (idx < contents.length && idx >= 0) {
            String content = contents[idx];
            if (!TextUtils.isEmpty(re)) {
                Matcher matcher = Pattern.compile(re).matcher(content);
                if (matcher.find()) {
                    content = matcher.group();
                }
            }
            return content;
        } else {
            return "";
        }
    }

    private static boolean[] getDuring(String duringRe, String rawDuring) {
        boolean[] weeks = new boolean[Constants.WEEKS];
        for (int i = 0; i < Constants.WEEKS; i++) {
            weeks[i] = false;
        }

        // 通过正则表达式提取有效信息
        String during = rawDuring;
        if (!TextUtils.isEmpty(duringRe)) {
            Matcher matcher = Pattern.compile(duringRe).matcher(rawDuring);
            if (matcher.find()) {
                during = matcher.group();
            }
        }

        int[] result = REHelper.getStartEnd(during);
        if (result[0] > 0 && result[0] <= Constants.WEEKS && result[1] >= result[0] && result[1] <= Constants.WEEKS) {
            for (int i = result[0] - 1; i < result[1]; i++) {
                weeks[i] = true;
            }
        }

        boolean odd = Pattern.compile("单周?").matcher(rawDuring).find();
        boolean even = Pattern.compile("双周?").matcher(rawDuring).find();

        if (odd) {
            for (int i = 1; i < 30; i += 2) {
                if (weeks[i]) {
                    weeks[i] = false;
                }
            }
        } else if (even) {
            for (int i = 0; i < 30; i += 2) {
                if (weeks[i]) {
                    weeks[i] = false;
                }
            }
        }
        return weeks;
    }

    public static boolean[] combineDuring(String duringRe, String rawDuring, @Nullable boolean[] old) {
        if (old == null) {
            return getDuring(duringRe, rawDuring);
        }

        boolean[] tmp = getDuring(duringRe, rawDuring);
        for (int i = 0; i < 30; i++) {
            if (tmp[i]) {
                old[i] = true;
            }
        }
        return old;
    }
}
