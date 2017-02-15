package cc.metapro.openct.data.source;

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

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Keep;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.university.UniversityInfo;

@Keep
class DBHelper extends SQLiteOpenHelper {

    static final String SCHOOL_NAME = "school_name";
    static final String CLASS_TABLE = "classes";
    static final String SCHOOL_TABLE = "schools";
    static final String GRADE_TABLE = "grades";
    static final String BORROW_TABLE = "borrows";
    static final String CUSTOM_TABLE = "custom";
    static final String ADV_CUSTOM_TABLE = "adv_custom";
    private static final String JSON = "json";
    private static final String DB_NAME = "openct.db";

    private static final Map<String, String> TITLE_TABLE_MAP = new HashMap<String, String>() {{
        put(SCHOOL_TABLE, "(" + SCHOOL_NAME + " TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(ADV_CUSTOM_TABLE, "(" + SCHOOL_NAME + " TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(CLASS_TABLE, "(id TEXT PRIMARY KEY, " + JSON + " TEXT)");
        put(GRADE_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
        put(BORROW_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
        put(CUSTOM_TABLE, "(id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)");
    }};

    private static final int DB_VERSION = 70;

    private Context mContext;

    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);

        updateSchools(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        createTables(db);

        updateSchools(db);
    }

    private void createTables(SQLiteDatabase db) {
        for (String title : TITLE_TABLE_MAP.keySet()) {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + title + TITLE_TABLE_MAP.get(title));
        }
    }

    private void updateSchools(SQLiteDatabase db) {
        try {
            String schools = StoreHelper.getAssetText(mContext, "school_info/schools.json");
            List<UniversityInfo> schoolInfoList = StoreHelper.fromJsonList(schools, UniversityInfo.class);

            db.beginTransaction();
            try {
                db.delete(DBHelper.SCHOOL_TABLE, null, null);
                for (UniversityInfo info : schoolInfoList) {
                    ContentValues values = new ContentValues();
                    values.put(SCHOOL_NAME, info.name);
                    values.put(JSON, info.toString());
                    db.insert(SCHOOL_TABLE, null, values);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
