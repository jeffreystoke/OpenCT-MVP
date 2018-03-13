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
import org.openct.android.data.entity.SchoolInfo

@Dao
interface SchoolDao {
    @Query("SELECT * FROM schools")
    fun list(): Flowable<SchoolInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(info: SchoolInfo)

    @Update
    fun update(info: SchoolInfo): Int

    @Query("DELETE FROM schools")
    fun clear()
}