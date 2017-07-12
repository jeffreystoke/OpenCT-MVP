package cc.metapro.openct.data.source.local

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

import cc.metapro.openct.utils.CloseUtils
import com.google.gson.Gson
import com.google.gson.JsonParser
import okio.Buffer
import java.io.*
import java.util.*

object StoreHelper {

    private val gson = Gson()

    fun toJson(o: Any): String {
        return gson.toJson(o)
    }

    fun <T> fromJsonList(jsonList: String, classType: Class<T>): List<T> {
        val array = JsonParser().parse(jsonList).asJsonArray
        val result = ArrayList<T>()
        for (element in array) {
            result.add(gson.fromJson(element, classType))
        }
        return result
    }

    internal fun <T> fromJson(jsonElement: String, tClass: Class<T>): T {
        return gson.fromJson(jsonElement, tClass)
    }

    @Throws(IOException::class)
    fun storeBytes(path: String, `in`: InputStream) {
        var out: DataOutputStream? = null
        try {
            out = DataOutputStream(FileOutputStream(path))
            val buffer = Buffer()
            buffer.readFrom(`in`).writeTo(out)
        } finally {
            CloseUtils.close(out)
        }
    }

    fun delFile(path: String) {
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}
