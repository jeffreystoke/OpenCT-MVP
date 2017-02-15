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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.scottyab.aescrypt.AESCrypt;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

@Keep
public class Loader {
    private static final String TAG = Loader.class.getSimpleName();

    private static boolean needUpdateUniversity;

    public static UniversityInfo university;

    public static void needUpdateUniversity() {
        needUpdateUniversity = true;
    }

    public static LibraryFactory getLibrary(Context context) {
        checkUniversity(context);
        return new LibraryFactory(university.libSys, university.libURL);
    }

    public static CmsFactory getCms(Context context) {
        checkUniversity(context);
        return new CmsFactory(university.cmsSys, university.cmsURL);
    }

    private static void checkUniversity(Context context) {
        if (university == null || needUpdateUniversity) {
            university = loadUniversity(context);
            assert university != null;
            needUpdateUniversity = false;
        }
    }

    @NonNull
    public static Map<String, String> getLibStuInfo(Context context) {
        Map<String, String> map = new HashMap<>(2);
        boolean needEncrypt = PrefHelper.getBoolean(context, R.string.pref_need_encryption);
        try {
            String password = PrefHelper.getString(context, R.string.pref_lib_password, "");
            if (needEncrypt) {
                password = AESCrypt.decrypt(Constants.seed, password);
            }
            if (!TextUtils.isEmpty(password)) {
                map.put(context.getString(R.string.key_username), PrefHelper.getString(context, R.string.pref_lib_username, ""));
                map.put(context.getString(R.string.key_password), password);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return map;
    }

    @NonNull
    public static Map<String, String> getCmsStuInfo(Context context) {
        Map<String, String> map = new HashMap<>(2);
        boolean needEncrypt = PrefHelper.getBoolean(context, R.string.pref_need_encryption);
        try {
            String password = PrefHelper.getString(context, R.string.pref_cms_password, "");
            if (needEncrypt) {
                password = AESCrypt.decrypt(Constants.seed, password);
            }
            if (!TextUtils.isEmpty(password)) {
                map.put(context.getString(R.string.key_username), PrefHelper.getString(context, R.string.pref_cms_username, ""));
                map.put(context.getString(R.string.key_password), password);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return map;
    }

    public static int getCurrentWeek(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(preferences.getString(context.getString(R.string.pref_current_week), "1"));
    }

    @Nullable
    private static UniversityInfo loadUniversity(final Context context) {
        DBManger manger = DBManger.getInstance(context);

        UniversityInfo university = null;
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable)) {
            university = manger.getCustomUniversity();
        } else {
            university = manger.getUniversity(PrefHelper.getString(context, R.string.pref_school_name, context.getResources().getStringArray(R.array.school_names)[0]));
        }

        if (university == null) {
            university = manger.getUniversity(PrefHelper.getString(context, R.string.pref_school_name, context.getResources().getStringArray(R.array.school_names)[0]));
        }

        updateWeekSeq(context);

        return university;
    }

    private static void updateWeekSeq(Context context) {
        try {
            int lastSetWeek = PrefHelper.getInt(context, R.string.pref_week_set_week);
            Calendar cal = Calendar.getInstance(Locale.CHINA);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            int weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
            int currentWeek = Integer.parseInt(PrefHelper.getString(context, R.string.pref_current_week, "1"));
            if (weekOfYearWhenSetCurrentWeek < lastSetWeek && lastSetWeek <= 53) {
                if (lastSetWeek == 53) {
                    currentWeek += weekOfYearWhenSetCurrentWeek;
                } else {
                    currentWeek += (52 - lastSetWeek) + weekOfYearWhenSetCurrentWeek;
                }
            } else {
                currentWeek += (weekOfYearWhenSetCurrentWeek - lastSetWeek);
            }
            if (currentWeek >= 30) {
                currentWeek = 1;
            }
            PrefHelper.putString(context, R.string.pref_current_week, currentWeek + "");
            PrefHelper.putInt(context, R.string.pref_week_set_week, weekOfYearWhenSetCurrentWeek);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
