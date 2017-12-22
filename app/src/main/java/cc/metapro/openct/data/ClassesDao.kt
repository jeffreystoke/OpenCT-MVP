/*
 * Copyright 2016 - 2017 OpenCT open source class table
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

package cc.metapro.openct.data

import android.arch.persistence.room.*
import openct.ClassInfo

@Dao
interface ClassesDao {
    @Query("SELECT * FROM Classes")
    fun classes(): List<ClassInfo>

    @Query("SELECT * FROM Classes WHERE name = :name")
    fun getClassByName(name: String): ClassInfo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertClass(classInfo: ClassInfo)

    @Update
    fun updateClass(classInfo: ClassInfo): Int

    @Query("DELETE FORM Classes WHERE name = :name")
    fun deleteClass(name: String): Int

    @Query("DELETE FROM Classes")
    fun deleteClasses()
}