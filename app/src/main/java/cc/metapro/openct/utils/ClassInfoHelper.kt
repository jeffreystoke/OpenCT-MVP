package cc.metapro.openct.utils

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

import android.text.TextUtils
import java.util.regex.Pattern

object ClassInfoHelper {

    fun getLength(time: String): Int {
        val tmp = REHelper.getStartEnd(time)
        return tmp[1] - tmp[0] + 1
    }

    fun infoParser(idx: Int, re: String, contents: Array<String>): String {
        return if (idx < contents.size && idx >= 0) {
            var content = contents[idx]
            if (!TextUtils.isEmpty(re)) {
                val matcher = Pattern.compile(re).matcher(content)
                if (matcher.find()) {
                    content = matcher.group()
                }
            }
            content
        } else {
            ""
        }
    }

    private fun getDuring(duringRe: String, rawDuring: String): BooleanArray {
        val weeks = BooleanArray(Constants.WEEKS)
        for (i in 0 until Constants.WEEKS) {
            weeks[i] = false
        }

        // 通过正则表达式提取有效信息
        var during = rawDuring
        if (!TextUtils.isEmpty(duringRe)) {
            val matcher = Pattern.compile(duringRe).matcher(rawDuring)
            if (matcher.find()) {
                during = matcher.group()
            }
        }

        val result = REHelper.getStartEnd(during)
        if (result[0] > 0 && result[0] <= Constants.WEEKS && result[1] >= result[0] && result[1] <= Constants.WEEKS) {
            for (i in result[0] - 1 until result[1]) {
                weeks[i] = true
            }
        }

        val odd = Pattern.compile("单周?").matcher(rawDuring).find()
        val even = Pattern.compile("双周?").matcher(rawDuring).find()

        if (odd) {
            var i = 1
            while (i < 30) {
                if (weeks[i]) {
                    weeks[i] = false
                }
                i += 2
            }
        } else if (even) {
            var i = 0
            while (i < 30) {
                if (weeks[i]) {
                    weeks[i] = false
                }
                i += 2
            }
        }
        return weeks
    }

    fun combineDuring(duringRe: String, rawDuring: String, old: BooleanArray?): BooleanArray {
        if (old == null) {
            return getDuring(duringRe, rawDuring)
        }

        val tmp = getDuring(duringRe, rawDuring)
        for (i in 0..29) {
            if (tmp[i]) {
                old[i] = true
            }
        }
        return old
    }
}
