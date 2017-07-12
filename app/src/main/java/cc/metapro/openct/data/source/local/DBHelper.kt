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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

internal class DBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        createTables(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {
        createTables(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        for (title in TITLE_TABLE_MAP.keys) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + title + TITLE_TABLE_MAP[title])
        }
    }

    companion object {

        val SCHOOL_NAME = "school_name"
        val CLASS_TABLE = "classes"
        val SCHOOL_TABLE = "schools"
        val GRADE_TABLE = "grades"
        val BORROW_TABLE = "borrows"
        val CUSTOM_TABLE = "custom"
        val DETAIL_CUSTOM_TABLE = "adv_custom"
        val JSON = "json"
        private val DB_NAME = "openct.db"

        private val TITLE_TABLE_MAP = object : HashMap<String, String>() {
            init {
                put(SCHOOL_TABLE, "($SCHOOL_NAME TEXT PRIMARY KEY, $JSON TEXT)")
                put(DETAIL_CUSTOM_TABLE, "($SCHOOL_NAME TEXT PRIMARY KEY, $JSON TEXT)")
                put(CLASS_TABLE, "(id TEXT PRIMARY KEY, $JSON TEXT)")
                put(GRADE_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, $JSON TEXT)")
                put(BORROW_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, $JSON TEXT)")
                put(CUSTOM_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, $JSON TEXT)")
            }
        }

        private val DB_VERSION = 90
    }
}
