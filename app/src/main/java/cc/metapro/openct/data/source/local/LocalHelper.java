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

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import org.jsoup.nodes.Document;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.LocalUser;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.model.BorrowInfo;
import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.REHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

public class LocalHelper {

    private static final String TAG = LocalHelper.class.getName();
    public static UniversityInfo university;
    private static boolean needUpdateUniversity;

    public static void needUpdateUniversity() {
        needUpdateUniversity = true;
    }

    public static UniversityInfo getUniversity(Context context) {
        checkUniversity(context);
        return university;
    }

    public static LibraryFactory getLibrary(Context context) {
        checkUniversity(context);
        return new LibraryFactory(university);
    }

    public static CmsFactory getCms(Context context) {
        checkUniversity(context);
        return new CmsFactory(university);
    }

    public static Observable<Document> login(final int actionType, final Context context, final String captcha) {
        return Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                Map<String, String> loginMap = new HashMap<>();
                if (actionType == Constants.TYPE_CMS) {
                    LocalUser localUser = getCmsStuInfo(context);
                    loginMap.put(Constants.USERNAME_KEY, localUser.getUsername());
                    loginMap.put(Constants.PASSWORD_KEY, localUser.getPassword());
                } else if (actionType == Constants.TYPE_LIB) {
                    LocalUser localUser = getLibStuInfo(context);
                    loginMap.put(Constants.USERNAME_KEY, localUser.getUsername());
                    loginMap.put(Constants.PASSWORD_KEY, localUser.getPassword());
                } else {
                    loginMap = new HashMap<>();
                }
                loginMap.put(Constants.CAPTCHA_KEY, captcha);
                checkUniversity(context);
                Document document = new CmsFactory(university).login(loginMap);
                e.onNext(document);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Classes> getClasses(final Context context) {
        return Observable.create(new ObservableOnSubscribe<Classes>() {
            @Override
            public void subscribe(ObservableEmitter<Classes> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                Classes allClasses = manger.getClasses();
                e.onNext(allClasses);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<GradeInfo>> getGrades(final Context context) {
        return Observable.create(new ObservableOnSubscribe<List<GradeInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                List<GradeInfo> grades = manger.getGrades();
                e.onNext(grades);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<List<BorrowInfo>> getBorrows(final Context context) {
        return Observable.create(new ObservableOnSubscribe<List<BorrowInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(context);
                List<BorrowInfo> borrowInfoList = manger.getBorrows();
                e.onNext(borrowInfoList);
            }
        }).subscribeOn(Schedulers.io());
    }

    public static Observable<Boolean> prepareOnlineInfo(final int actionType, final Context context) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                if (actionType == Constants.TYPE_CMS) {
                    checkUniversity(context);
                    e.onNext(new CmsFactory(university).prepareOnlineInfo());
                } else if (actionType == Constants.TYPE_LIB) {
                    e.onNext(new LibraryFactory(university).prepareOnlineInfo());
                }
            }
        }).subscribeOn(Schedulers.io());
    }

    private static void checkUniversity(Context context) {
        if (university == null || needUpdateUniversity) {
            university = loadUniversity(context);
            needUpdateUniversity = false;
        }
    }

    @NonNull
    public static LocalUser getLibStuInfo(Context context) {
        String username = PrefHelper.getString(context, R.string.pref_lib_username, "");
        String password = PrefHelper.getString(context, R.string.pref_lib_password, "");
        return new LocalUser(username, password);
    }

    @NonNull
    public static LocalUser getCmsStuInfo(Context context) {
        String username = PrefHelper.getString(context, R.string.pref_cms_username, "");
        String password = PrefHelper.getString(context, R.string.pref_cms_password, "");
        return new LocalUser(username, password);
    }

    public static int getCurrentWeek(Context context) {
        updateWeekSeq(context);
        return Integer.parseInt(PrefHelper.getString(context, R.string.pref_current_week, "1"));
    }

    @NonNull
    private static UniversityInfo loadUniversity(final Context context) {
        DBManger manger = DBManger.getInstance(context);

        final String defaultSchoolName = context.getString(R.string.default_school_name);
        UniversityInfo university;
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable, false)) {
            university = manger.getCustomUniversity();
        } else {
            university = manger.getUniversity(PrefHelper.getString(context, R.string.pref_school_name, defaultSchoolName));
        }

        return university;
    }

    private static void updateWeekSeq(Context context) {
        try {
            Calendar cal = Calendar.getInstance(Locale.CHINA);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            // 当前周是年度第几周
            int weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR);

            // 上次设置本周的时候是年度第几周
            int lastSetWeek = Integer.parseInt(PrefHelper.getString(context, R.string.pref_week_set_week, weekOfYearWhenSetCurrentWeek + ""));
            // 上次存储的是学期第几周
            int currentWeek = Integer.parseInt(PrefHelper.getString(context, R.string.pref_current_week, "1"));

            if (weekOfYearWhenSetCurrentWeek < lastSetWeek && lastSetWeek <= 53) {
                // 跨年的情况
                if (lastSetWeek == 53) {
                    // 上次设置的时候是一年的最后一周
                    currentWeek += weekOfYearWhenSetCurrentWeek;
                } else {
                    // 上次设置的时候不是最后一周
                    currentWeek += (52 - lastSetWeek) + weekOfYearWhenSetCurrentWeek;
                }
            } else {
                // 在年内
                currentWeek += (weekOfYearWhenSetCurrentWeek - lastSetWeek);
            }

            if (currentWeek > 30) {
                currentWeek = 1;
            }

            PrefHelper.putString(context, R.string.pref_current_week, currentWeek + "");
            PrefHelper.putString(context, R.string.pref_week_set_week, weekOfYearWhenSetCurrentWeek + "");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static SparseArray<Calendar> getClassTime(Context context) {
        int size = Integer.parseInt(PrefHelper.getString(context, R.string.pref_daily_class_count, "12"));
        SparseArray<Calendar> result = new SparseArray<>(size);
        for (int i = 0; i < size; i++) {
            String def = "";
            if (i < 10) {
                def += "0";
            }
            String time = PrefHelper.getString(context, Constants.TIME_PREFIX + i, def + i + ":00");
            Calendar calendar = Calendar.getInstance();
            int[] parts = REHelper.getUserSetTime(time);
            calendar.set(Calendar.HOUR_OF_DAY, parts[0]);
            calendar.set(Calendar.MINUTE, parts[1]);
            calendar.set(Calendar.SECOND, 0);
            result.put(i + 1, calendar);
        }
        return result;
    }

}
