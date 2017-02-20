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

    @NonNull
    public static AdvancedCustomInfo getAdvancedCustomInfo(Context context) {
        DBManger.getInstance(context);
        Cursor cursor = null;
        String name;
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable)) {
            name = PrefHelper.getString(context, R.string.pref_custom_school_name, "openct");
        } else {
            name = PrefHelper.getString(context, R.string.pref_school_name, context.getResources().getStringArray(R.array.school_names)[0]);
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

    public void updateAdvancedCustomClassInfo(AdvancedCustomInfo info) {
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

    public void delAdvancedCustomClassInfo() {
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

    @Nullable
    UniversityInfo getCustomUniversity() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(
                    DBHelper.CUSTOM_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            return StoreHelper.fromJson(cursor.getString(1), UniversityInfo.class);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Nullable
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

    public void updateClasses(@Nullable Classes classes) {
        mDatabase.beginTransaction();
        try {
            mDatabase.delete(DBHelper.CLASS_TABLE, null, null);
            if (classes != null) {
                for (EnrichedClassInfo c : classes) {
                    String target = c.toString();
                    if (!TextUtils.isEmpty(target)) {
                        mDatabase.execSQL(
                                "INSERT INTO " + DBHelper.CLASS_TABLE + " VALUES(?, ?)",
                                new Object[]{c.getName(), target}
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
    public List<GradeInfo> getGrades() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.GRADE_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            List<GradeInfo> grades = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                grades.add(StoreHelper.fromJson(cursor.getString(1), GradeInfo.class));
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
    public List<BorrowInfo> getBorrows() {
        Cursor cursor = null;
        try {
            cursor = mDatabase.query(DBHelper.BORROW_TABLE, null, null, null, null, null, null);
            cursor.moveToFirst();
            List<BorrowInfo> grades = new ArrayList<>();
            while (!cursor.isAfterLast()) {
                grades.add(StoreHelper.fromJson(cursor.getString(1), BorrowInfo.class));
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
