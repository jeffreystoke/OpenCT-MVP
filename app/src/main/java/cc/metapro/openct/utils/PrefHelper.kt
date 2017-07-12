package cc.metapro.openct.utils

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
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PrefHelper {

    private var mPref: SharedPreferences? = null

    fun putInt(context: Context, resId: Int, value: Int) {
        putInt(context, context.getString(resId), value)
    }

    fun putInt(context: Context, prefKey: String, value: Int) {
        checkPref(context)
        val editor = mPref!!.edit()
        editor.putInt(prefKey, value)
        editor.apply()
    }

    fun putString(context: Context, resId: Int, value: String) {
        putString(context, context.getString(resId), value)
    }

    fun putString(context: Context, prefKey: String, value: String) {
        checkPref(context)
        val editor = mPref!!.edit()
        editor.putString(prefKey, value)
        editor.apply()
    }

    fun putBoolean(context: Context, resId: Int, value: Boolean) {
        putBoolean(context, context.getString(resId), value)
    }

    fun putBoolean(context: Context, prefKey: String, value: Boolean) {
        checkPref(context)
        val editor = mPref!!.edit()
        editor.putBoolean(prefKey, value)
        editor.apply()
    }

    fun getBoolean(context: Context, resId: Int, defaultValue: Boolean): Boolean {
        return getBoolean(context, context.getString(resId), defaultValue)
    }

    fun getBoolean(context: Context, prefKey: String, defaultValue: Boolean): Boolean {
        checkPref(context)
        return mPref!!.getBoolean(prefKey, defaultValue)
    }

    fun getString(context: Context, resId: Int, defaultValue: String): String {
        return getString(context, context.getString(resId), defaultValue)
    }

    fun getString(context: Context, prefKey: String, defaultValue: String): String {
        checkPref(context)
        return mPref!!.getString(prefKey, defaultValue)
    }

    fun getInt(context: Context, resId: Int, defaultValue: Int): Int {
        return getInt(context, context.getString(resId), defaultValue)
    }

    fun getInt(context: Context, prefKey: String, defaultValue: Int): Int {
        checkPref(context)
        return mPref!!.getInt(prefKey, defaultValue)
    }

    private fun checkPref(context: Context) {
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(context)
        }
    }
}
