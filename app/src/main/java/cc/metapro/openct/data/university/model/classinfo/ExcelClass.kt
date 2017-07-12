package cc.metapro.openct.data.university.model.classinfo

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

import cc.metapro.openct.utils.ClassInfoHelper
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.DateHelper
import cc.metapro.openct.utils.REHelper

class ExcelClass {

    private val name: String? = null
    private val type: String? = null
    private val time: String? = null
    private val during: String? = null
    private val teacher: String? = null
    private val place: String? = null

    private fun getDuring(): BooleanArray {
        val tmp = during!!.split("&".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var during = BooleanArray(Constants.WEEKS)
        for (s in tmp) {
            during = ClassInfoHelper.combineDuring("", s, during)
        }
        return during
    }

    val enrichedClassInfo: EnrichedClassInfo
        get() = EnrichedClassInfo(name, type, getTime())

    private fun getTime(): ClassTime {
        val startEnd = REHelper.getStartEnd(this.time)
        return ClassTime(DateHelper.chineseToWeekDay(this.time), startEnd[0], startEnd[1], teacher, place, getDuring())
    }
}
