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

import cc.metapro.openct.XLSXReader
import net.lingala.zip4j.exception.ZipException
import org.json.JSONException
import org.json.JSONStringer
import java.io.IOException

object ExcelHelper {

    fun xlsxToTable(path: String): String {
        try {
            val reader = XLSXReader(path)
            return reader.sheets[0]
        } catch (e: ZipException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return ""
    }

    @Throws(JSONException::class)
    fun tableToJson(table: String): String {
        val rows = table.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val tableContents = arrayOfNulls<Array<String>>(rows.size)
        var i = 0
        rows
                .map { row -> row.split("\t".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() }
                .forEach { tableContents[i++] = it }

        var headers: Array<String>? = null
        if (tableContents.isNotEmpty()) {
            headers = tableContents[0]
        }

        if (headers == null) {
            return ""
        }

        val stringer = JSONStringer()
        stringer.array()
        i = 1
        while (i < tableContents.size) {
            stringer.`object`()
            var j = 0
            while (j < tableContents[i]!!.size && j < headers.size) {
                stringer.key(headers[j].trim { it <= ' ' }).value(tableContents[i]!![j])
                j++
            }
            stringer.endObject()
            i++
        }
        stringer.endArray()

        return stringer.toString()
    }
}
