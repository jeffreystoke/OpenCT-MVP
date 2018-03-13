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

package org.openct.android.data

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.TypeConverters
import org.openct.android.data.dao.BorrowDao
import org.openct.android.data.dao.ClassDao
import org.openct.android.data.dao.GradeDao
import org.openct.android.data.dao.SchoolDao
import org.openct.android.data.entity.BorrowInfo
import org.openct.android.data.entity.ClassInfo
import org.openct.android.data.entity.GradeInfo
import org.openct.android.data.entity.SchoolInfo
import org.openct.android.utils.Converters

@Database(entities = [ClassInfo::class, GradeInfo::class, BorrowInfo::class, SchoolInfo::class], version = 1)
@TypeConverters(value = [Converters::class])
abstract class AppDB : RoomDatabase() {
    abstract fun classDao(): ClassDao
    abstract fun gradeDao(): GradeDao
    abstract fun borrowDao(): BorrowDao
    abstract fun schoolDao(): SchoolDao
}