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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;

@Keep
public class DBManger {

    private static final String TAG = DBManger.class.getSimpleName();

    private static SQLiteDatabase mDatabase;

    private static DBManger manger;

    private DBManger(Context context) {
        DBHelper DBHelper = new DBHelper(context);
        mDatabase = DBHelper.getWritableDatabase();
    }

    public static DBManger getInstance(Context context) {
        synchronized (DBManger.class) {
            if (manger == null) {
                synchronized (DBManger.class) {
                    manger = new DBManger(context);
                }
            }
        }
        return manger;
    }

    /**
     * 更新高级自定义信息
     *
     * @param info 新的高级自定义信息
     */
    public void updateAdvancedCustomClassInfo(AdvancedCustomInfo info) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.ADV_CUSTOM_TABLE, null, null);
            String json = info.toString();
            mDatabase.execSQL("INSERT INTO " + DBHelper.ADV_CUSTOM_TABLE + " VALUES(null, ?)", new Object[]{json});
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * 获取高级自定义信息
     *
     * @return
     */
    public AdvancedCustomInfo getAdvancedCustomInfo() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.ADV_CUSTOM_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            Gson gson = new Gson();
            return gson.fromJson(cursor.getString(1), AdvancedCustomInfo.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 更新基础自定义的学校基本信息
     *
     * @param info 新的自定义学校信息
     */
    public void updateCustomSchoolInfo(UniversityInfo.SchoolInfo info) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.CUSTOM_TABLE, null, null);
            String json = info.toString();
            mDatabase.execSQL("INSERT INTO " + DBHelper.CUSTOM_TABLE + " VALUES(null, ?)", new Object[]{json});
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * 获取自定义学校的基本信息
     *
     * @return 自定义学校的基本信息
     */
    UniversityInfo.SchoolInfo getCustomSchoolInfo() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.CUSTOM_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            Gson gson = new Gson();
            return gson.fromJson(cursor.getString(1), UniversityInfo.SchoolInfo.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * 根据学校中文名称获取学校基本信息
     *
     * @param name 学校名称
     * @return 学校基本信息
     */
    private UniversityInfo.SchoolInfo getSchoolInfo(String name) {
        Cursor cursor = mDatabase.query(
                DBHelper.SCHOOL_TABLE, null,
                DBHelper.SCHOOL_NAME + "=? COLLATE NOCASE", new String[]{name},
                null, null, null);
        int n = cursor.getColumnCount();
        String[] content = cursor.getColumnNames();
        Map<String, String> strKvs = new HashMap<>(n);
        Map<String, Boolean> boolKvs = new HashMap<>(n);
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            for (int i = 0; i < n; i++) {
                switch (cursor.getType(i)) {
                    case Cursor.FIELD_TYPE_STRING:
                        strKvs.put(content[i], cursor.getString(i));
                        break;
                    case Cursor.FIELD_TYPE_INTEGER:
                        boolean s = cursor.getInt(i) == 1;
                        boolKvs.put(content[i], s);
                        break;
                }
            }
        }
        cursor.close();

        return new UniversityInfo.SchoolInfo(strKvs, boolKvs);
    }

    /**
     * 根据学校基本信息返回学校完整信息
     *
     * @param schoolInfo 学校基本信息
     * @return 学校完整信息
     */
    UniversityInfo getUniversity(UniversityInfo.SchoolInfo schoolInfo) {
        UniversityInfo universityInfo = new UniversityInfo();
        String cmsJson;
        String libJson;


        Gson gson = new Gson();
        Cursor cmsCursor = null;
        try {
            cmsCursor = mDatabase.query(
                    DBHelper.CMS_TABLE, null,
                    DBHelper.SYS_NAME + "=? COLLATE NOCASE",
                    new String[]{schoolInfo.cmsSys}, null, null, null
            );
            cmsCursor.moveToFirst();
            cmsJson = cmsCursor.getString(2);
            cmsCursor.close();
            UniversityInfo.CMSInfo cmsInfo = gson.fromJson(cmsJson, UniversityInfo.CMSInfo.class);
            cmsInfo.mCmsURL = schoolInfo.cmsURL;
            cmsInfo.mDynLoginURL = schoolInfo.cmsDynURL;
            cmsInfo.mNeedCAPTCHA = schoolInfo.cmsCaptcha;
            universityInfo.mCMSInfo = cmsInfo;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cmsCursor != null) {
                cmsCursor.close();
            }
        }

        Cursor libCursor = null;
        try {
            libCursor = mDatabase.query(
                    DBHelper.LIB_TABLE, null,
                    DBHelper.SYS_NAME + "=? COLLATE NOCASE",
                    new String[]{schoolInfo.libSys}, null, null, null
            );
            libCursor.moveToFirst();
            libJson = libCursor.getString(2);
            libCursor.close();
            UniversityInfo.LibraryInfo libraryInfo = gson.fromJson(libJson, UniversityInfo.LibraryInfo.class);
            libraryInfo.mLibURL = schoolInfo.libURL;
            libraryInfo.mNeedCAPTCHA = schoolInfo.libCaptcha;
            universityInfo.mLibraryInfo = libraryInfo;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (libCursor != null) {
                libCursor.close();
            }
        }
        return universityInfo;
    }

    /**
     * 根据学校中文名称返回学校信息
     *
     * @param name 学校名称 (全称, 中文)
     * @return 学校完整信息
     */
    UniversityInfo getUniversity(String name) {
        UniversityInfo.SchoolInfo schoolInfo = getSchoolInfo(name);
        return getUniversity(schoolInfo);
    }

    /**
     * 更新课程信息
     *
     * @param classes 新的课程信息
     */
    public void updateClasses(@NonNull List<EnrichedClassInfo> classes) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.CLASS_TABLE, null, null);
            for (EnrichedClassInfo c : classes) {
                mDatabase.execSQL(
                        "INSERT INTO " + DBHelper.CLASS_TABLE + " VALUES(null, ?)",
                        new Object[]{c.toString()}
                );
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    public List<EnrichedClassInfo> getClassInfos() {
        Cursor cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<EnrichedClassInfo> classInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            classInfos.add(gson.fromJson(cursor.getString(1), EnrichedClassInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return classInfos;
    }

    /**
     * 更新成绩信息
     *
     * @param grades 新的成绩信息
     */
    public void updateGradeInfos(@NonNull List<GradeInfo> grades) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.GRADE_TABLE, null, null);
            for (GradeInfo g : grades) {
                mDatabase.execSQL(
                        "INSERT INTO " + DBHelper.GRADE_TABLE + " VALUES(null, ?)",
                        new Object[]{g.toString()}
                );
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    public List<GradeInfo> getGradeInfos() {
        Cursor cursor = mDatabase.query(DBHelper.GRADE_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<GradeInfo> gradeInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            gradeInfos.add(gson.fromJson(cursor.getString(1), GradeInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return gradeInfos;
    }

    /**
     * 更新借阅信息
     *
     * @param borrow 新的借阅信息
     */
    public void updateBorrowInfos(List<BorrowInfo> borrow) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.BORROW_TABLE, null, null);
            if (borrow != null) {
                for (BorrowInfo b : borrow) {
                    mDatabase.execSQL(
                            "INSERT INTO " + DBHelper.BORROW_TABLE + " VALUES(null, ?)",
                            new Object[]{b.toString()}
                    );
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    public List<BorrowInfo> getBorrowInfos() {
        Cursor cursor = mDatabase.query(DBHelper.BORROW_TABLE, null, null, null, null, null, null);
        cursor.moveToFirst();
        List<BorrowInfo> borrowInfos = new ArrayList<>();
        Gson gson = new Gson();
        while (!cursor.isAfterLast()) {
            borrowInfos.add(gson.fromJson(cursor.getString(1), BorrowInfo.class));
            cursor.moveToNext();
        }
        cursor.close();
        return borrowInfos;
    }
}
