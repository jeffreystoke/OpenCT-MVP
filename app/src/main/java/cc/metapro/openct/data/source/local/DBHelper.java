package cc.metapro.openct.data.source.local;

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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

class DBHelper extends SQLiteOpenHelper {

    static final String SCHOOL_NAME = "school_name";
    static final String CLASS_TABLE = "classes";
    static final String SCHOOL_TABLE = "schools";
    static final String GRADE_TABLE = "grades";
    static final String BORROW_TABLE = "borrows";
    static final String CUSTOM_TABLE = "custom";
    static final String DETAIL_CUSTOM_TABLE = "adv_custom";
    static final String JSON = "json";
    private static final String DB_NAME = "openct.db";

    private static final Map<String, String> TITLE_TABLE_MAP = new HashMap<String, String>() {{
        put(SCHOOL_TABLE, "(" + SCHOOL_NAME + " TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(DETAIL_CUSTOM_TABLE, "(" + SCHOOL_NAME + " TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(CLASS_TABLE, "(id TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(GRADE_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
        put(BORROW_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
        put(CUSTOM_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
    }};

    private static final int DB_VERSION = 90;


    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        for (String title : TITLE_TABLE_MAP.keySet()) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + title + TITLE_TABLE_MAP.get(title));
        }
    }
}
