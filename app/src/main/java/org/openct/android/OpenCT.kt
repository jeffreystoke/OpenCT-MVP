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

package org.openct.android

import android.app.Application
import android.arch.persistence.room.Room
import com.blankj.utilcode.util.CrashUtils
import com.blankj.utilcode.util.Utils
import org.openct.android.data.AppDB

class OpenCT : Application() {

    companion object {
        lateinit var sDB: AppDB
    }

    override fun onCreate() {
        super.onCreate()

        Utils.init(this)
        CrashUtils.init()

        sDB = Room.databaseBuilder(
                applicationContext,
                AppDB::class.java,
                "openct-db")
                .build()
    }
}