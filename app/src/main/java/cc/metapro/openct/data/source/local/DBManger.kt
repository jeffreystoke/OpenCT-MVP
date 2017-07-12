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

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.text.TextUtils
import android.util.Log
import cc.metapro.openct.R
import cc.metapro.openct.data.university.ClassTableInfo
import cc.metapro.openct.data.university.DetailCustomInfo
import cc.metapro.openct.data.university.UniversityInfo
import cc.metapro.openct.data.university.model.BorrowInfo
import cc.metapro.openct.data.university.model.GradeInfo
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo
import cc.metapro.openct.utils.CloseUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import net.fortuna.ical4j.validate.ValidationException
import java.util.*

class DBManger private constructor(context: Context) {

    init {
        val DBHelper = DBHelper(context)
        mDatabase = DBHelper.writableDatabase
    }

    fun updateAdvCustomInfo(info: DetailCustomInfo) {
        mDatabase.beginTransaction()
        try {
            val json = info.toString()
            mDatabase.delete(DBHelper.DETAIL_CUSTOM_TABLE, null, null)
            if (!TextUtils.isEmpty(json)) {
                mDatabase.execSQL("INSERT INTO " + DBHelper.DETAIL_CUSTOM_TABLE + " VALUES(?, ?)", arrayOf<Any>(info.schoolName, json))
            }
            mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        } finally {
            mDatabase.endTransaction()
        }
    }

    fun delAdvancedCustomInfo() {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBHelper.DETAIL_CUSTOM_TABLE, null, null)
            mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        } finally {
            mDatabase.endTransaction()
        }
    }

    fun updateCustomSchoolInfo(info: UniversityInfo) {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBHelper.CUSTOM_TABLE, null, null)
            val json = info.toString()
            mDatabase.execSQL("INSERT INTO " + DBHelper.CUSTOM_TABLE + " VALUES(null, ?)", arrayOf<Any>(json))
            mDatabase.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        } finally {
            mDatabase.endTransaction()
        }
    }

    internal val customUniversity: UniversityInfo
        get() {
            var cursor: Cursor? = null
            try {
                cursor = mDatabase.query(
                        DBHelper.CUSTOM_TABLE, null, null, null, null, null, null)
                cursor!!.moveToFirst()
                var universityInfo: UniversityInfo? = null
                if (!cursor.isAfterLast) {
                    universityInfo = StoreHelper.fromJson(cursor.getString(1), UniversityInfo::class.java)
                }
                if (universityInfo == null) {
                    universityInfo = DEFAULT_UNIVERSITY
                }
                cursor.close()
                return universityInfo
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                return DEFAULT_UNIVERSITY
            } finally {
                CloseUtils.close(cursor)
            }
        }

    internal fun getUniversity(name: String): UniversityInfo {
        var cursor: Cursor? = null
        try {
            cursor = mDatabase.query(
                    DBHelper.SCHOOL_TABLE, null,
                    DBHelper.SCHOOL_NAME + "=? COLLATE NOCASE", arrayOf(name), null, null, null)
            cursor!!.moveToFirst()
            var info: UniversityInfo? = null
            if (!cursor.isAfterLast) {
                info = StoreHelper.fromJson(cursor.getString(1), UniversityInfo::class.java)
            }
            if (info == null) {
                info = DEFAULT_UNIVERSITY
            }
            cursor.close()
            return info
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return DEFAULT_UNIVERSITY
        } finally {
            CloseUtils.close(cursor)
        }
    }

    /**
     * return a class info according to the given name, if not exists, return null

     * @param name class name
     */
    fun getSingleClass(name: String): EnrichedClassInfo? {
        var cursor: Cursor? = null
        try {
            if (TextUtils.isEmpty(name)) {
                throw Exception("school class name must not be empty")
            }
            cursor = mDatabase.query(DBHelper.CLASS_TABLE, null,
                    "id=? COLLATE NOCASE", arrayOf(name), null, null, null)
            cursor!!.moveToFirst()
            var info: EnrichedClassInfo? = null
            if (!cursor.isAfterLast) {
                info = StoreHelper.fromJson(cursor.getString(1), EnrichedClassInfo::class.java)
            }
            cursor.close()
            return info
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            return null
        } finally {
            CloseUtils.close(cursor)
        }
    }

    /**
     * update the class info you have edited, no duplicated name please

     * @param oldName old name of edited class info, null if it's a new one
     * *
     * @param newName new name of edited class info, should not be empty
     * *
     * @param info    edited class info
     * *
     * @throws Exception if the edited class info with a empty name
     */
    @Throws(ValidationException::class)
    fun updateSingleClass(oldName: String?, newName: String, info: EnrichedClassInfo?) {
        mDatabase.beginTransaction()
        var cursor: Cursor? = null
        try {
            cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, "id=? COLLATE NOCASE", arrayOf(newName), null, null, null)
            cursor!!.moveToFirst()

            // name not modified
            if (!TextUtils.isEmpty(oldName) && oldName == newName) {
                if (info != null) {
                    val values = ContentValues()
                    values.put("id", oldName)
                    values.put(DBHelper.JSON, info.toString())
                    mDatabase.update(DBHelper.CLASS_TABLE, values, "id=? COLLATE NOCASE", arrayOf(oldName))
                }
            } else if (!cursor.isAfterLast) {
                // name modified and new name is duplicated
                throw ValidationException("Duplicate class name, won't be accepted!")
            } else {
                // name modified and new name is valid
                if (!TextUtils.isEmpty(oldName)) {
                    mDatabase.delete(DBHelper.CLASS_TABLE, "id=? COLLATE NOCASE", arrayOf(oldName!!))
                }

                if (info != null) {
                    val values = ContentValues()
                    values.put("id", newName)
                    values.put(DBHelper.JSON, info.toString())
                    mDatabase.insert(DBHelper.CLASS_TABLE, null, values)
                }
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
            CloseUtils.close(cursor)
        }
    }

    /**
     * update all class you have got

     * @param classes all classes you have got
     */
    fun updateClasses(classes: Classes) {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBHelper.CLASS_TABLE, null, null)
            for (c in classes) {
                val target = c.toString()
                if (!TextUtils.isEmpty(target)) {
                    val contentValues = ContentValues(2)
                    contentValues.put("id", c.name)
                    contentValues.put(DBHelper.JSON, target)
                    mDatabase.insert(DBHelper.CLASS_TABLE, null, contentValues)
                }
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    /**
     * get all classes you have stored
     */
    val classes: Classes
        get() {
            var cursor: Cursor? = null
            try {
                cursor = mDatabase.query(DBHelper.CLASS_TABLE, null, null, null, null, null, null)
                cursor!!.moveToFirst()
                val classes = Classes()
                while (!cursor.isAfterLast) {
                    classes.add(StoreHelper.fromJson(cursor.getString(1), EnrichedClassInfo::class.java))
                    cursor.moveToNext()
                }
                cursor.close()
                return classes
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                return Classes()
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }

    fun updateGrades(grades: List<GradeInfo>?) {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBHelper.GRADE_TABLE, null, null)
            if (grades != null) {
                for (g in grades) {
                    val target = g.toString()
                    if (!TextUtils.isEmpty(target)) {
                        mDatabase.execSQL(
                                "INSERT INTO " + DBHelper.GRADE_TABLE + " VALUES(null, ?)",
                                arrayOf<Any>(target)
                        )
                    }
                }
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    internal val grades: List<GradeInfo>
        get() {
            var cursor: Cursor? = null
            try {
                cursor = mDatabase.query(DBHelper.GRADE_TABLE, null, null, null, null, null, null)
                cursor!!.moveToFirst()
                val grades = ArrayList<GradeInfo>()
                while (!cursor.isAfterLast) {
                    grades.add(StoreHelper.fromJson(cursor.getString(1), GradeInfo::class.java))
                    cursor.moveToNext()
                }
                cursor.close()
                return grades
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
            return ArrayList(0)
        }

    fun updateBorrows(borrow: List<BorrowInfo>?) {
        mDatabase.beginTransaction()
        try {
            mDatabase.delete(DBHelper.BORROW_TABLE, null, null)
            if (borrow != null) {
                for (b in borrow) {
                    val target = b.toString()
                    if (!TextUtils.isEmpty(target)) {
                        mDatabase.execSQL(
                                "INSERT INTO " + DBHelper.BORROW_TABLE + " VALUES(null, ?)",
                                arrayOf<Any>(target)
                        )
                    }
                }
            }
            mDatabase.setTransactionSuccessful()
        } finally {
            mDatabase.endTransaction()
        }
    }

    internal val borrows: List<BorrowInfo>
        get() {
            var cursor: Cursor? = null
            try {
                cursor = mDatabase.query(DBHelper.BORROW_TABLE, null, null, null, null, null, null)
                cursor!!.moveToFirst()
                val grades = ArrayList<BorrowInfo>()
                while (!cursor.isAfterLast) {
                    grades.add(StoreHelper.fromJson(cursor.getString(1), BorrowInfo::class.java))
                    cursor.moveToNext()
                }
                cursor.close()
                return grades
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
            return ArrayList(0)
        }

    val schools: List<UniversityInfo>
        get() {
            var cursor: Cursor? = null
            try {
                cursor = mDatabase.query(DBHelper.SCHOOL_TABLE, null, null, null, null, null, null)
                cursor!!.moveToFirst()
                val grades = ArrayList<UniversityInfo>()
                while (!cursor.isAfterLast) {
                    grades.add(StoreHelper.fromJson(cursor.getString(1), UniversityInfo::class.java))
                    cursor.moveToNext()
                }
                return grades
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
            return ArrayList(0)
        }

    companion object {

        private val TAG = DBManger::class.java.name
        private val DEFAULT_UNIVERSITY = UniversityInfo()
        private lateinit var mDatabase: SQLiteDatabase
        private var manger: DBManger? = null

        fun getInstance(context: Context): DBManger? {
            synchronized(DBManger::class.java) {
                if (manger == null) {
                    synchronized(DBManger::class.java) {
                        manger = DBManger(context)
                    }
                }
            }
            return manger
        }

        fun getDetailCustomInfo(context: Context): DetailCustomInfo {
            DBManger.getInstance(context)
            var cursor: Cursor? = null
            val name: String

            // get user's school name, as a key of custom info
            if (PrefHelper.getBoolean(context, R.string.pref_custom_enable, false)) {
                name = PrefHelper.getString(context, R.string.pref_custom_school_name, Constants.DEFAULT_SCHOOL_NAME)
            } else {
                name = PrefHelper.getString(context, R.string.pref_school_name, Constants.DEFAULT_SCHOOL_NAME)
            }

            try {
                // select custom info according to school name
                cursor = mDatabase.query(DBHelper.DETAIL_CUSTOM_TABLE, null,
                        String.format("%s=? COLLATE NOCASE", DBHelper.SCHOOL_NAME),
                        arrayOf(name), null, null, null)

                cursor!!.moveToFirst()
                // only the first should be selected
                if (!cursor.isAfterLast) {
                    val detailCustomInfo = StoreHelper.fromJson(cursor.getString(1), DetailCustomInfo::class.java)
                    detailCustomInfo.setClassTableInfo(ClassTableInfo.getDefault(context))
                    return detailCustomInfo
                } else {
                    throw Exception("need init advancedCustomInfo")
                }
            } catch (e: Exception) {
                Log.v(TAG, e.message, e)
                val detailCustomInfo = DetailCustomInfo(name)
                detailCustomInfo.setClassTableInfo(ClassTableInfo.getDefault(context))
                return detailCustomInfo
            } finally {
                if (cursor != null) {
                    cursor.close()
                }
            }
        }

        fun updateSchools(context: Context, universityInfoList: List<UniversityInfo>?) {
            DBManger.getInstance(context)
            mDatabase.beginTransaction()
            try {
                mDatabase.delete(DBHelper.SCHOOL_TABLE, null, null)
                if (universityInfoList != null) {
                    for (info in universityInfoList) {
                        val values = ContentValues()
                        values.put(DBHelper.SCHOOL_NAME, info.name)
                        values.put(DBHelper.JSON, info.toString())
                        mDatabase.insert(DBHelper.SCHOOL_TABLE, null, values)
                    }
                }
                mDatabase.setTransactionSuccessful()
            } finally {
                mDatabase.endTransaction()
            }
        }
    }
}
