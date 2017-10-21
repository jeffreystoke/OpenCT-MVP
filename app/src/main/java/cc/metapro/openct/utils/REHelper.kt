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

object REHelper {

    private val timeDuringPattern = "([0-2]?[0-9])"
    private val lastNumberPattern = "([0-2]?[0-9])(?=[^0-9]*$)"
    private val emptyPattern = "^\\s+$"

    /**
     * 获取一节课的开始和结束时间, 课程周期, 范围 0 - 29
     */
    fun getStartEnd(s: String?): IntArray {
        if (TextUtils.isEmpty(s)) {
            return intArrayOf(-1, -1)
        }
        val first = Pattern.compile(timeDuringPattern).matcher(s!!)
        if (first.find()) {
            val sS = first.group()
            if (!TextUtils.isEmpty(sS)) {
                val start = Integer.parseInt(first.group())
                val last = Pattern.compile(lastNumberPattern).matcher(s)
                var end = start
                if (last.find()) {
                    val eS = last.group()
                    if (!TextUtils.isEmpty(eS)) {
                        end = Integer.parseInt(eS)
                    }
                }
                return intArrayOf(start, end)
            } else {
                return intArrayOf(-1, -1)
            }
        } else {
            return intArrayOf(-1, -1)
        }
    }

    /**
     * 判断字符串是否是空串或空白串
     * @param s 字符创
     * @return 是否是空串或空白串
     */
    fun isEmpty(s: String): Boolean {
        return TextUtils.isEmpty(s) || Pattern.compile(emptyPattern).matcher(s).find()
    }

    fun getUserSetTime(s: String): IntArray {
        val tmp = s.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val result = intArrayOf(0, 0)
        var i = 0
        while (i < result.size && i < tmp.size) {
            result[i] = Integer.parseInt(tmp[i])
            i++
        }
        return result
    }
}
