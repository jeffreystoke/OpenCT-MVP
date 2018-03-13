/*
 * Copyright 2016 - 2018 OpenCT open source class table
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openct.android.utils

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import org.openct.android.data.entity.ClassInfo

class Converters {
    @TypeConverter
    fun toBoolArray(string: String): Array<Boolean> {
        return Gson().fromJson(string, Array<Boolean>::class.java)
    }

    @TypeConverter
    fun fromBoolArray(array: Array<Boolean>): String {
        return Gson().toJson(array)
    }
}
