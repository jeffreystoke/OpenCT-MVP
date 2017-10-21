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

import org.jsoup.nodes.Element
import java.text.SimpleDateFormat
import java.util.*

class BorrowInfo(th: Element, tr: Element) : KeyValueModel(th, tr) {

    fun isExceeded(toDay: Date): Boolean {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        try {
            val date = format.parse(mTitleValueMap["应还日期"])
            return date.before(toDay)
        } catch (ignored: Exception) {

        }

        return false
    }
}
