package cc.metapro.openct.data.source;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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
import android.support.annotation.NonNull;

import com.google.common.base.Strings;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.UniversityService;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.EncryptionUtils;

public class Loader {

    private static UniversityInfo university;
    private static UniversityService service;

    public static LibraryFactory getLibrary() {
        checkService();
        return new LibraryFactory(service, university.mLibraryInfo);
    }

    public static CmsFactory getCms() {
        checkService();
        return new CmsFactory(service, university.mCMSInfo);
    }

    private static void checkService() {
        if (service == null) {
            service = ServiceGenerator.createService(UniversityService.class, ServiceGenerator.HTML);
        }
    }

    @NonNull
    public static Map<String, String> getLibStuInfo(Context context) {
        Map<String, String> map = new HashMap<>(2);
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_LIB_PASSWORD_KEY, "");

            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            if (!Strings.isNullOrEmpty(decryptedCode)) {
                map.put(Constants.USERNAME_KEY, preferences.getString(Constants.PREF_LIB_USERNAME_KEY, ""));
                map.put(Constants.PASSWORD_KEY, decryptedCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @NonNull
    public static Map<String, String> getCmsStuInfo(Context context) {
        Map<String, String> map = new HashMap<>(2);
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String password = preferences.getString(Constants.PREF_CMS_PASSWORD_KEY, "");
            String decryptedCode = EncryptionUtils.decrypt(Constants.seed, password);
            if (!Strings.isNullOrEmpty(decryptedCode)) {
                map.put(Constants.USERNAME_KEY, preferences.getString(Constants.PREF_CMS_USERNAME_KEY, ""));
                map.put(Constants.PASSWORD_KEY, decryptedCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static int getDailyClasses() {
        return university.mCMSInfo.mClassTableInfo.mDailyClasses;
    }

    public static int getClassLength() {
        return university.mCMSInfo.mClassTableInfo.mClassLength;
    }

    public static int getCurrentWeek(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(preferences.getString(Constants.PREF_CURRENT_WEEK_KEY, "1"));
    }

    public static boolean cmsNeedCAPTCHA() {
        return university.mCMSInfo.mNeedCAPTCHA;
    }

    public static boolean libNeedCAPTCHA() {
        return university.mLibraryInfo.mNeedCAPTCHA;
    }

    public static void loadUniversity(final Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            boolean custom = preferences.getBoolean(Constants.PREF_USE_CUSTOM, false);
            DBManger manger = DBManger.getInstance(context);
            if (custom) {
                UniversityInfo.SchoolInfo info = manger.getCustomSchoolInfo();
                if (info != null) {
                    university = manger.getUniversity(info);
                } else {
                    university = manger.getUniversity(preferences.getString(Constants.PREF_SCHOOL_ABBR_KEY, context.getResources().getStringArray(R.array.pref_school_values)[0]));
                }
            } else {
                university = manger.getUniversity(preferences.getString(Constants.PREF_SCHOOL_ABBR_KEY, context.getResources().getStringArray(R.array.pref_school_values)[0]));
            }

            // update current week
            int lastSetWeek = Integer.parseInt(preferences.getString(Constants.PREF_WEEK_SET_KEY, "1"));
            Calendar cal = Calendar.getInstance(Locale.CHINA);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            int weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
            int currentWeek = Integer.parseInt(preferences.getString(Constants.PREF_CURRENT_WEEK_KEY, "1"));
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
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.PREF_CURRENT_WEEK_KEY, currentWeek + "");
            editor.putString(Constants.PREF_WEEK_SET_KEY, weekOfYearWhenSetCurrentWeek + "");
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
