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

package org.openct.android.data.dao

import android.arch.persistence.room.*
import io.reactivex.Flowable
import org.openct.android.data.entity.ClassInfo

@Dao
interface ClassDao {
    @Query("SELECT * FROM classes GROUP BY name")
    fun list(): Flowable<ClassInfo>

    @Query("SELECT * FROM classes WHERE name = :arg0")
    fun get(name: String): Flowable<ClassInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(info: ClassInfo)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun update(info: ClassInfo): Int

    @Delete
    fun delete(vararg info: ClassInfo): Int

    @Query("DELETE FROM classes WHERE name = :arg0")
    fun delete(name: String): Int

    @Query("DELETE FROM classes")
    fun clear()
}