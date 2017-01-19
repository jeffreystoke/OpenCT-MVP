package cc.metapro.openct.data.source;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import android.util.Log;

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
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Loader {
    private static final String TAG = "LOADER";

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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean needEncrypt = preferences.getBoolean(context.getString(R.string.need_encryption), false);
        try {
            String password = preferences.getString(context.getString(R.string.pref_lib_password), "");
            if (needEncrypt) {
                password = EncryptionUtils.decrypt(Constants.seed, password);
            }
            if (!Strings.isNullOrEmpty(password)) {
                map.put(Constants.USERNAME_KEY, preferences.getString(context.getString(R.string.pref_lib_username), ""));
                map.put(Constants.PASSWORD_KEY, password);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return map;
    }

    @NonNull
    public static Map<String, String> getCmsStuInfo(Context context) {
        Map<String, String> map = new HashMap<>(2);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean needEncrypt = preferences.getBoolean(context.getString(R.string.need_encryption), false);
        try {
            String password = preferences.getString(context.getString(R.string.pref_cms_password), "");
            if (needEncrypt) {
                password = EncryptionUtils.decrypt(Constants.seed, password);
            }
            if (!Strings.isNullOrEmpty(password)) {
                map.put(Constants.USERNAME_KEY, preferences.getString(context.getString(R.string.pref_cms_username), ""));
                map.put(Constants.PASSWORD_KEY, password);
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return map;
    }

    public static int getClassLength() {
        try {
            return university.mCMSInfo.mClassTableInfo.mClassLength;
        } catch (Exception e) {
            return 0;
        }
    }

    public static int getDailyClasses() {
        try {
            return university.mCMSInfo.mClassTableInfo.mDailyClasses;
        } catch (Exception e) {
            return 0;
        }
    }

    public static boolean cmsNeedCAPTCHA() {
        try {
            return university.mCMSInfo.mNeedCAPTCHA;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean libNeedCAPTCHA() {
        try {
            return university.mLibraryInfo.mNeedCAPTCHA;
        } catch (Exception e) {
            return false;
        }
    }

    public static int getCurrentWeek(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String week = preferences.getString(context.getString(R.string.pref_current_week), "第1周");
        return Integer.parseInt(week.replaceAll("[^\\x00-\\xff]", ""));
    }

    public static void loadUniversity(final Context context) {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                        boolean custom = preferences.getBoolean(context.getString(R.string.pref_custom_enable), false);
                        DBManger manger = DBManger.getInstance(context);
                        String defaultSchool = context.getResources().getStringArray(R.array.school_names)[0];
                        if (custom) {
                            UniversityInfo.SchoolInfo info = manger.getCustomSchoolInfo();
                            if (info != null) {
                                university = manger.getUniversity(info);
                            } else {
                                university = manger.getUniversity(preferences.getString(context.getString(R.string.pref_school_name), defaultSchool));
                            }
                        } else {
                            university = manger.getUniversity(preferences.getString(context.getString(R.string.pref_school_name), defaultSchool));
                        }

                        int lastSetWeek = Integer.parseInt(preferences.getString(context.getString(R.string.pref_week_set_week), "1"));
                        Calendar cal = Calendar.getInstance(Locale.CHINA);
                        cal.setFirstDayOfWeek(Calendar.MONDAY);
                        int weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);
                        String week = preferences.getString(context.getString(R.string.pref_current_week), "第1周");
                        int currentWeek = Integer.parseInt(week.replaceAll("[^\\x00-\\xff]", ""));
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
                        editor.putString(context.getString(R.string.pref_current_week), "第" + currentWeek + "周");
                        editor.putString(context.getString(R.string.pref_week_set_week), weekOfYearWhenSetCurrentWeek + "");
                        editor.apply();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        return "";
                    }
                }).subscribe();
    }

}
