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

package org.openct.android.data.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import java.util.*

@Entity(tableName = "classes", primaryKeys = ["name", "weeks", "week_day"])
class ClassInfo {
    var name: String = ""
    var type: String = ""
    var teacher: String = ""
    var place: String = ""
    var weeks: Array<Boolean> = emptyArray()
    @ColumnInfo(name = "week_day")
    var weekDay: Int = -1
    @ColumnInfo(name = "daily_start")
    var dailyStart: Int = -1
    @ColumnInfo(name = "daily_end")
    var dailyEnd: Int = -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ClassInfo

        if (name != other.name) return false
        if (type != other.type) return false
        if (teacher != other.teacher) return false
        if (place != other.place) return false
        if (!Arrays.equals(weeks, other.weeks)) return false
        if (weekDay != other.weekDay) return false
        if (dailyStart != other.dailyStart) return false
        if (dailyEnd != other.dailyEnd) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + teacher.hashCode()
        result = 31 * result + place.hashCode()
        result = 31 * result + Arrays.hashCode(weeks)
        result = 31 * result + weekDay
        result = 31 * result + dailyStart
        result = 31 * result + dailyEnd
        return result
    }
}