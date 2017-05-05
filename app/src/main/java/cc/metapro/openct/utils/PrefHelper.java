package cc.metapro.openct.utils;

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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefHelper {

    private static SharedPreferences mPref;

    public static void putInt(Context context, int resId, int value) {
        putInt(context, context.getString(resId), value);
    }

    public static void putInt(Context context, String prefKey, int value) {
        checkPref(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(prefKey, value);
        editor.apply();
    }

    public static void putString(Context context, int resId, String value) {
        putString(context, context.getString(resId), value);
    }

    public static void putString(Context context, String prefKey, String value) {
        checkPref(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(prefKey, value);
        editor.apply();
    }

    public static void putBoolean(Context context, int resId, boolean value) {
        putBoolean(context, context.getString(resId), value);
    }

    public static void putBoolean(Context context, String prefKey, boolean value) {
        checkPref(context);
        SharedPreferences.Editor editor = mPref.edit();
        editor.putBoolean(prefKey, value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, int resId, boolean defaultValue) {
        return getBoolean(context, context.getString(resId), defaultValue);
    }

    public static boolean getBoolean(Context context, String prefKey, boolean defaultValue) {
        checkPref(context);
        return mPref.getBoolean(prefKey, defaultValue);
    }

    public static String getString(Context context, int resId, String defaultValue) {
        return getString(context, context.getString(resId), defaultValue);
    }

    public static String getString(Context context, String prefKey, String defaultValue) {
        checkPref(context);
        return mPref.getString(prefKey, defaultValue);
    }

    public static int getInt(Context context, int resId, int defaultValue) {
        return getInt(context, context.getString(resId), defaultValue);
    }

    public static int getInt(Context context, String prefKey, int defaultValue) {
        checkPref(context);
        return mPref.getInt(prefKey, defaultValue);
    }

    private static void checkPref(Context context) {
        if (mPref == null) {
            mPref = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }
}
