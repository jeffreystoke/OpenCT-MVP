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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.data.university.UniversityInfo;

public class DBHelper extends SQLiteOpenHelper {

    public static final String
            SCHOOL_NAME = "school_name",
            CMS_SYS = "cms_sys", CMS_URL = "cms_url", CMS_CAPTCHA = "cms_captcha",
            CMS_DYN_URL = "cms_dyn_url", CMS_INNER_ACCESS = "cms_inner_access",
            LIB_SYS = "lib_sys", LIB_URL = "lib_url", LIB_CAPTCHA = "lib_captcha",
            LIB_DYN_URL = "lib_dyn_url", LIB_INNER_ACCESS = "lib_inner_access";

    static final String CLASS_TABLE = "classes";
    static final String SCHOOL_TABLE = "schools";
    static final String SYS_NAME = "sys_name";
    static final String CMS_TABLE = "cmss";
    static final String LIB_TABLE = "libs";
    static final String GRADE_TABLE = "grades";
    static final String BORROW_TABLE = "borrows";
    static final String CUSTOM_TABLE = "custom";
    static final String ADV_CUSTOM_TABLE = "adv_custom";
    private static final String JSON = "json";
    private static final String DB_NAME = "openct-1.db";

    private static final String SCHOOL_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    SCHOOL_NAME + " TEXT, " +
                    CMS_SYS + " TEXT, " + CMS_URL + " TEXT, " +
                    CMS_DYN_URL + " BOOLEAN, " + CMS_CAPTCHA + " BOOLEAN, " +
                    CMS_INNER_ACCESS + " BOOLEAN, " +
                    LIB_SYS + " TEXT, " + LIB_URL + " TEXT, " +
                    LIB_DYN_URL + " BOOLEAN, " + LIB_CAPTCHA + " BOOLEAN, " +
                    LIB_INNER_ACCESS + " BOOLEAN)";

    private static final String LIB_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + SYS_NAME + " TEXT, " + JSON + " TEXT)";
    private static final String CMS_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + SYS_NAME + " TEXT, " + JSON + " TEXT)";
    private static final String CLASS_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)";
    private static final String GRADE_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)";
    private static final String BORROW_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)";
    private static final String CUSTOM_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)";
    private static final String ADV_CUSTOM_TITLES =
            "(_id INTEGER PRIMARY KEY AUTOINCREMENT, " + JSON + " TEXT)";

    private static final int DB_VERSION = 27;

    private Context mContext;

    DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);

        updateSchools(db);
        updateCmsSys(db);
        updateLibSys(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        createTables(db);

        updateSchools(db);
        updateCmsSys(db);
        updateLibSys(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + SCHOOL_TABLE + SCHOOL_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CLASS_TABLE + CLASS_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + GRADE_TABLE + GRADE_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + BORROW_TABLE + BORROW_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CMS_TABLE + CMS_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + LIB_TABLE + LIB_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + CUSTOM_TABLE + CUSTOM_TITLES);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + ADV_CUSTOM_TABLE + ADV_CUSTOM_TITLES);
    }

    private void updateSchools(SQLiteDatabase db) {
        try {
            String schools = StoreHelper.getAssetText(mContext, "schools.json");
            JsonArray jsonArray = new JsonParser().parse(schools).getAsJsonArray();
            Gson gson = new Gson();
            List<UniversityInfo.SchoolInfo> schoolInfos = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                UniversityInfo.SchoolInfo schoolInfo = gson.fromJson(element, UniversityInfo.SchoolInfo.class);
                schoolInfos.add(schoolInfo);
            }

            db.beginTransaction();
            try {
                db.delete(DBHelper.SCHOOL_TABLE, null, null);
                for (UniversityInfo.SchoolInfo info : schoolInfos) {
                    db.execSQL(
                            "INSERT INTO " + DBHelper.SCHOOL_TABLE +
                                    " VALUES(null, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                            new Object[]{
                                    info.name, info.cmsSys, info.cmsURL,
                                    info.cmsDynURL, info.cmsCaptcha, info.cmsInnerAccess,
                                    info.libSys, info.libURL,
                                    info.libDynURL, info.libCaptcha, info.libInnerAccess
                            }
                    );
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCmsSys(SQLiteDatabase db) {
        try {
            String cmss = StoreHelper.getAssetText(mContext, "cms.json");
            JsonArray jsonArray = new JsonParser().parse(cmss).getAsJsonArray();
            Gson gson = new Gson();
            List<UniversityInfo.CMSInfo> cmsInfos = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                UniversityInfo.CMSInfo cms = gson.fromJson(element, UniversityInfo.CMSInfo.class);
                cmsInfos.add(cms);
            }

            db.beginTransaction();
            try {
                db.delete(DBHelper.CMS_TABLE, null, null);
                for (UniversityInfo.CMSInfo info : cmsInfos) {
                    db.execSQL(
                            "INSERT INTO " + DBHelper.CMS_TABLE + " VALUES(null, ?, ?)",
                            new Object[]{info.mCmsSys, info.toString()}
                    );
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateLibSys(SQLiteDatabase db) {
        try {
            String libs = StoreHelper.getAssetText(mContext, "lib.json");
            JsonArray jsonArray = new JsonParser().parse(libs).getAsJsonArray();
            Gson gson = new Gson();
            List<UniversityInfo.LibraryInfo> libraries = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                UniversityInfo.LibraryInfo lib = gson.fromJson(element, UniversityInfo.LibraryInfo.class);
                libraries.add(lib);
            }

            db.beginTransaction();
            try {
                db.delete(DBHelper.LIB_TABLE, null, null);
                for (UniversityInfo.LibraryInfo info : libraries) {
                    db.execSQL(
                            "INSERT INTO " + DBHelper.LIB_TABLE + " VALUES(null, ?, ?)",
                            new Object[]{info.mLibSys, info.toString()}
                    );
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
