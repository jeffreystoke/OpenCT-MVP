package cc.metapro.openct.data.university.model

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

import cc.metapro.openct.utils.REHelper
import org.jsoup.nodes.Element
import java.util.*

open class KeyValueModel constructor(th: Element, tr: Element) {

    internal var mTitleValueMap: LinkedHashMap<String, String> = LinkedHashMap()

    fun getFilteredContent(filter: List<String>?): String {
        if (filter != null) {
            Collections.sort(filter)
        }

        val sb = StringBuilder()
        if (!mTitleValueMap.isEmpty()) {
            for (key in mTitleValueMap.keys) {
                if (filter != null) {
                    val i = Collections.binarySearch(filter, key)
                    if (i >= 0) {
                        sb.append(key).append(": ").append(mTitleValueMap[key]).append("\n\n")
                    }
                } else {
                    sb.append(key).append(": ").append(mTitleValueMap[key]).append("\n\n")
                }
            }

            if (sb[sb.length - 1] == '\n') {
                sb.replace(sb.length - 2, sb.length, "")
            }
        }
        return sb.toString()
    }

    val titles: Collection<String>
        get() = mTitleValueMap.keys

    fun toFullString(): String {
        val sb = StringBuilder()
        for (key in mTitleValueMap.keys) {
            sb.append(key).append(": ").append(mTitleValueMap[key]).append("\n\n")
        }
        if (sb.length > 2) {
            if (sb[sb.length - 1] == '\n') {
                sb.replace(sb.length - 2, sb.length, "")
            }
        }
        return sb.toString()
    }

    init {
        var titles = th.select("td")
        if (titles.isEmpty()) {
            titles = th.select("th")
        }
        val values = tr.select("td")
        var i = 0
        for (title in titles) {
            val value = values[i++].text()
            if (!REHelper.isEmpty(value)) {
                mTitleValueMap.put(title.text(), value)
            }
        }
    }
}

class GradeInfo(th: Element, tr: Element) : KeyValueModel(th, tr)

//class BookInfo(th: Element, tr: Element) : KeyValueModel(th, tr)