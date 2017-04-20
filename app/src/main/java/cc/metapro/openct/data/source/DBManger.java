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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.PrefHelper;

public class DBManger {

    private static final String TAG = DBManger.class.getSimpleName();
    private static final UniversityInfo DEFAULT_UNIVERSITY =
            new UniversityInfo("OpenCT-Default", "common", "example.com", "common", "example.com");
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

    @NonNull
    public static AdvancedCustomInfo getAdvancedCustomInfo(Context context) {
        DBManger.getInstance(context);
        Cursor cursor = null;
        String name;
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable)) {
            name = PrefHelper.getString(context, R.string.pref_custom_school_name, "openct");
        } else {
            name = PrefHelper.getString(context, R.string.pref_school_name, context.getString(R.string.default_school_name));
        }
        try {
            cursor = mDatabase.query(
                    DBHelper.ADV_CUSTOM_TABLE, null,
                    DBHelper.SCHOOL_NAME + "=? COLLATE NOCASE", new String[]{name},
                    null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                AdvancedCustomInfo customInfo = StoreHelper.fromJson(cursor.getString(1), AdvancedCustomInfo.class);
                if (customInfo.mClassTableInfo == null) {
                    customInfo.mClassTableInfo = new CmsFactory.ClassTableInfo();
                }

                customInfo.mClassTableInfo.mNameRE = PrefHelper.getString(context, R.string.pref_class_name_re, "");
                customInfo.mClassTableInfo.mTypeRE = PrefHelper.getString(context, R.string.pref_class_type_re, "");
                customInfo.mClassTableInfo.mDuringRE = PrefHelper.getString(context, R.string.pref_class_during_re, "\\d+-\\d+");
                customInfo.mClassTableInfo.mTimeRE = PrefHelper.getString(context, R.string.pref_class_time_re, "(\\d+,)+\\d+");
                customInfo.mClassTableInfo.mPlaceRE = PrefHelper.getString(context, R.string.pref_class_place_re, "");
                customInfo.mClassTableInfo.mTeacherRE = PrefHelper.getString(context, R.string.pref_class_teacher_re, "");
                return customInfo;
            } else {
                throw new Exception("need init advancedCustomInfo");
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage(), e);
            AdvancedCustomInfo customInfo = new AdvancedCustomInfo(context);
            customInfo.mClassTableInfo = new CmsFactory.ClassTableInfo();
            customInfo.mClassTableInfo.mNameRE = PrefHelper.getString(context, R.string.pref_class_name_re, "");
            customInfo.mClassTableInfo.mTypeRE = PrefHelper.getString(context, R.string.pref_class_type_re, "");
            customInfo.mClassTableInfo.mDuringRE = PrefHelper.getString(context, R.string.pref_class_during_re, "\\d+-\\d+");
            customInfo.mClassTableInfo.mTimeRE = PrefHelper.getString(context, R.string.pref_class_time_re, "(\\d+,)+\\d+");
            customInfo.mClassTableInfo.mPlaceRE = PrefHelper.getString(context, R.string.pref_class_place_re, "");
            customInfo.mClassTableInfo.mTeacherRE = PrefHelper.getString(context, R.string.pref_class_teacher_re, "");
            return customInfo;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static void updateSchools(Context context, @Nullable List<UniversityInfo> universityInfoList) {
        DBManger.getInstance(context);
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.SCHOOL_TABLE, null, null);
            if (universityInfoList != null) {
                for (UniversityInfo info : universityInfoList) {
                    ContentValues values = new ContentValues();
                    values.put(DBHelper.SCHOOL_NAME, info.name);
                    values.put(DBHelper.JSON, info.toString());
                    mDatabase.insert(DBHelper.SCHOOL_TABLE, null, values);
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void updateAdvCustomInfo(AdvancedCustomInfo info) {
        mDatabase.beginTransaction();
        try {
            String json = info.toString();
            mDatabase.delete(DBHelper.ADV_CUSTOM_TABLE, null, null);
            if (!TextUtils.isEmpty(json)) {
                mDatabase.execSQL("INSERT INTO " + DBHelper.ADV_CUSTOM_TABLE + " VALUES(?, ?)", new Object[]{info.getSchoolName(), json});
            }
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void delAdvancedCustomInfo() {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.ADV_CUSTOM_TABLE, null, null);
            mDatabase.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void updateCustomSchoolInfo(UniversityInfo info) {
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

    @NonNull
    UniversityInfo getCustomUniversity() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(
                    DBHelper.CUSTOM_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            UniversityInfo universityInfo = null;
            if (!cursor.isAfterLast()) {
                universityInfo = StoreHelper.fromJson(cursor.getString(1), UniversityInfo.class);
            }
            if (universityInfo == null) {
                universityInfo = DEFAULT_UNIVERSITY;
            }
            cursor.close();
            return universityInfo;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return DEFAULT_UNIVERSITY;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @NonNull
    UniversityInfo getUniversity(String name) {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(
                    DBHelper.SCHOOL_TABLE, null,
                    DBHelper.SCHOOL_NAME + "=? COLLATE NOCASE", new String[]{name},
                    null, null, null);
            cursor.moveToFirst();
            UniversityInfo info = null;
            if (!cursor.isAfterLast()) {
                info = StoreHelper.fromJson(cursor.getString(1), UniversityInfo.class);
            }
            if (info == null) {
                info = DEFAULT_UNIVERSITY;
            }
            cursor.close();
            return info;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return DEFAULT_UNIVERSITY;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * return a class info according to the given name, if not exists, return null
     *
     * @param name class name
     */
    @Nullable
    public EnrichedClassInfo getSingleClass(String name) {
        Cursor cursor = null;
        try {
            if (TextUtils.isEmpty(name)) {
                throw new Exception("school class name must not be empty");
            }
            cursor = mDatabase.query(DBHelper.CLASS_TABLE, null,
                    "id=? COLLATE NOCASE", new String[]{name},
                    null, null, null);
            cursor.moveToFirst();
            EnrichedClassInfo info = null;
            if (!cursor.isAfterLast()) {
                info = StoreHelper.fromJson(cursor.getString(1), EnrichedClassInfo.class);
            }
            cursor.close();
            return info;
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
     * update the class info you have edited, no duplicated name please
     *
     * @param oldName old name of edited class info, null if it's a new one
     * @param newName new name of edited class info, should not be empty
     * @param info    edited class info
     * @throws Exception if the edited class info with a empty name
     */
    public void updateSingleClass(@Nullable String oldName, @NonNull String newName, EnrichedClassInfo info) throws Exception {
        mDatabase.beginTransaction();
        Cursor cursor = null;
        try {
            if (!TextUtils.isEmpty(oldName)) {
                mDatabase.delete(DBHelper.CLASS_TABLE, "id=? COLLATE NOCASE", new String[]{oldName});
            }

            // check if the new name exists
            cursor = mDatabase.query(
                    DBHelper.CLASS_TABLE, null,
                    "id=? COLLATE NOCASE", new String[]{newName},
                    null, null, null);
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                throw new Exception("Duplicate class name, won't be accepted!");
            } else {
                ContentValues values = new ContentValues();
                values.put("id", newName);
                values.put(DBHelper.JSON, info.toString());
                mDatabase.insert(DBHelper.CLASS_TABLE, null, values);
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * update all class you have got
     *
     * @param classes all classes you have got
     */
    public void updateClasses(@Nullable Classes classes) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.CLASS_TABLE, null, null);
            if (classes != null) {
                for (EnrichedClassInfo c : classes) {
                    String target = c.toString();
                    if (!TextUtils.isEmpty(target)) {
                        ContentValues contentValues = new ContentValues(2);
                        contentValues.put("id", c.getName());
                        contentValues.put(DBHelper.JSON, target);
                        mDatabase.insert(DBHelper.CLASS_TABLE, null, contentValues);
                    }
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    /**
     * get all classes you have stored
     */
    @NonNull
    public Classes getClasses() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            Classes classes = new Classes();
            while (!cursor.isAfterLast()) {
                classes.add(StoreHelper.fromJson(cursor.getString(1), EnrichedClassInfo.class));
                cursor.moveToNext();
            }
            cursor.close();
            return classes;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return new Classes();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void updateGrades(@Nullable List<GradeInfo> grades) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.GRADE_TABLE, null, null);
            if (grades != null) {
                for (GradeInfo g : grades) {
                    String target = g.toString();
                    if (!TextUtils.isEmpty(target)) {
                        mDatabase.execSQL(
                                "INSERT INTO " + DBHelper.GRADE_TABLE + " VALUES(null, ?)",
                                new Object[]{target}
                        );
                    }
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    List<GradeInfo> getGrades() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.GRADE_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            List<GradeInfo> grades = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                grades.add(StoreHelper.fromJson(cursor.getString(1), GradeInfo.class));
                cursor.moveToNext();
            }
            cursor.close();
            return grades;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new ArrayList<>(0);
    }

    public void updateBorrows(@Nullable List<BorrowInfo> borrow) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.BORROW_TABLE, null, null);
            if (borrow != null) {
                for (BorrowInfo b : borrow) {
                    String target = b.toString();
                    if (!TextUtils.isEmpty(target)) {
                        mDatabase.execSQL(
                                "INSERT INTO " + DBHelper.BORROW_TABLE + " VALUES(null, ?)",
                                new Object[]{target}
                        );
                    }
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    @NonNull
    List<BorrowInfo> getBorrows() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.BORROW_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            List<BorrowInfo> grades = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                grades.add(StoreHelper.fromJson(cursor.getString(1), BorrowInfo.class));
                cursor.moveToNext();
            }
            cursor.close();
            return grades;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new ArrayList<>(0);
    }

    @NonNull
    public List<UniversityInfo> getSchools() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.SCHOOL_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            List<UniversityInfo> grades = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                grades.add(StoreHelper.fromJson(cursor.getString(1), UniversityInfo.class));
                cursor.moveToNext();
            }
            return grades;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return new ArrayList<>(0);
    }
}
