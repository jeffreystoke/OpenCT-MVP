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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import net.fortuna.ical4j.validate.ValidationException;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.university.ClassTableInfo;
import cc.metapro.openct.data.university.DetailCustomInfo;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.model.BorrowInfo;
import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.CloseUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

public class DBManger {

    private static final String TAG = DBManger.class.getName();
    private static final UniversityInfo DEFAULT_UNIVERSITY = new UniversityInfo();
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
    public static DetailCustomInfo getDetailCustomInfo(Context context) {
        DBManger.getInstance(context);
        Cursor cursor = null;
        String name;

        // get user's school name, as a key of custom info
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable, false)) {
            name = PrefHelper.getString(context, R.string.pref_custom_school_name, Constants.DEFAULT_SCHOOL_NAME);
        } else {
            name = PrefHelper.getString(context, R.string.pref_school_name, Constants.DEFAULT_SCHOOL_NAME);
        }

        try {
            // select custom info according to school name
            cursor = mDatabase.query(DBHelper.DETAIL_CUSTOM_TABLE, null,
                    String.format("%s=? COLLATE NOCASE", DBHelper.SCHOOL_NAME),
                    new String[]{name}, null, null, null);

            cursor.moveToFirst();
            // only the first should be selected
            if (!cursor.isAfterLast()) {
                DetailCustomInfo detailCustomInfo = StoreHelper.fromJson(cursor.getString(1), DetailCustomInfo.class);
                if (detailCustomInfo.mClassTableInfo == null) {
                    detailCustomInfo.setClassTableInfo(ClassTableInfo.getDefault(context));
                }
                return detailCustomInfo;
            } else {
                throw new Exception("need init advancedCustomInfo");
            }
        } catch (Exception e) {
            Log.v(TAG, e.getMessage(), e);
            DetailCustomInfo detailCustomInfo = new DetailCustomInfo(name);
            detailCustomInfo.setClassTableInfo(ClassTableInfo.getDefault(context));
            return detailCustomInfo;
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
                    values.put(DBHelper.SCHOOL_NAME, info.getName());
                    values.put(DBHelper.JSON, info.toString());
                    mDatabase.insert(DBHelper.SCHOOL_TABLE, null, values);
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
        }
    }

    public void updateAdvCustomInfo(DetailCustomInfo info) {
        mDatabase.beginTransaction();
        try {
            String json = info.toString();
            mDatabase.delete(DBHelper.DETAIL_CUSTOM_TABLE, null, null);
            if (!TextUtils.isEmpty(json)) {
                mDatabase.execSQL("INSERT INTO " + DBHelper.DETAIL_CUSTOM_TABLE + " VALUES(?, ?)", new Object[]{info.getSchoolName(), json});
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
            mDatabase.delete(DBHelper.DETAIL_CUSTOM_TABLE, null, null);
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
            CloseUtils.close(cursor);
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
            CloseUtils.close(cursor);
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
            CloseUtils.close(cursor);
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
    public void updateSingleClass(@Nullable String oldName, @NonNull String newName, EnrichedClassInfo info) throws ValidationException {
        mDatabase.beginTransaction();
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, "id=? COLLATE NOCASE", new String[]{newName}, null, null, null);
            cursor.moveToFirst();

            // name not modified
            if (!TextUtils.isEmpty(oldName) && oldName.equals(newName)) {
                if (info != null) {
                    ContentValues values = new ContentValues();
                    values.put("id", oldName);
                    values.put(DBHelper.JSON, info.toString());
                    mDatabase.update(DBHelper.CLASS_TABLE, values, "id=? COLLATE NOCASE", new String[]{oldName});
                }
            } else if (!cursor.isAfterLast()) {
                // name modified and new name is duplicated
                throw new ValidationException("Duplicate class name, won't be accepted!");
            } else {
                // name modified and new name is valid
                if (!TextUtils.isEmpty(oldName)) {
                    mDatabase.delete(DBHelper.CLASS_TABLE, "id=? COLLATE NOCASE", new String[]{oldName});
                }

                if (info != null) {
                    ContentValues values = new ContentValues();
                    values.put("id", newName);
                    values.put(DBHelper.JSON, info.toString());
                    mDatabase.insert(DBHelper.CLASS_TABLE, null, values);
                }
            }
            mDatabase.setTransactionSuccessful();
        } finally {
            mDatabase.endTransaction();
            CloseUtils.close(cursor);
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
