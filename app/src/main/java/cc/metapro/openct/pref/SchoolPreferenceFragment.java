package cc.metapro.openct.pref;

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

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.splash.schoolselection.SchoolSelectionActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.REHelper;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class SchoolPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = SchoolPreferenceFragment.class.getName();
    private Preference mSchoolPreference;
    private Preference mCurrentWeekPreference;
    private Preference mCmsPasswordPreference;
    private Preference mLibPasswordPreference;
    private SwitchPreference mCustomEnablePreference;
    private Preference mCustomSchoolNamePreference;
    private Preference mDailyClassCountPreference;
    private PreferenceScreen mClassSettingScreen;
    private List<Preference> mPreferences;

    private List<Preference> mTimePreferences;

    public SchoolPreferenceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_school);
        setHasOptionsMenu(false);
        ButterKnife.bind(this, getActivity());
        mPreferences = new ArrayList<>();
        mTimePreferences = new ArrayList<>();

        setUpdatePreference();
        bindPreferences();
        bindListener();
    }

    private void setUpdatePreference() {
        Preference checkSchoolUpdate = findPreference(getString(R.string.pref_check_school_info_update));
        checkSchoolUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("注意")
                        .setMessage("即将从开源课程表的 GitHub仓库获取最新学校信息, 是否继续?")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(getActivity(), R.string.loading_university_info_list);
                                progressDialog.show();

                                Observable<List<UniversityInfo>> observable = Observable.create(new ObservableOnSubscribe<List<UniversityInfo>>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<List<UniversityInfo>> e) throws Exception {
                                        List<UniversityInfo> universityInfoList = ServiceGenerator
                                                .createOpenCTService().getOnlineUniversityInfo().execute().body();
                                        if (universityInfoList == null || universityInfoList.isEmpty()) {
                                            e.onError(new Exception("获取仓库内校园信息失败, 请检查网络环境"));
                                        } else {
                                            e.onNext(universityInfoList);
                                        }
                                    }
                                });
                                Observer<List<UniversityInfo>> observer = new MyObserver<List<UniversityInfo>>(TAG) {
                                    @Override
                                    public void onNext(final List<UniversityInfo> universityList) {
                                        progressDialog.dismiss();
                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("提示")
                                                .setMessage("共有 " + universityList.size() + " 条学校信息, 是否替换当前所有学校信息")
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        DBManger.updateSchools(getActivity(), universityList);
                                                        Toast.makeText(getActivity(), "更新成功, 请到 设置 -> 学校 搜索并选择你想要的学校", Toast.LENGTH_LONG).show();
                                                    }
                                                }).show();
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        super.onError(e);
                                        progressDialog.dismiss();
                                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                };

                                observable.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(observer);
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null).show();
                return true;
            }
        });
    }

    private void bindPreferences() {
        mDailyClassCountPreference = findPreference(getString(R.string.pref_daily_class_count));
        mPreferences.add(mDailyClassCountPreference);

        mClassSettingScreen = (PreferenceScreen) findPreference(getString(R.string.pref_class_settings));
        addTimePreferences();

        findPreference(getString(R.string.pref_custom_action_clear)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("提示")
                        .setMessage("这将清空你的操作记录, 是否继续?")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                DBManger manger = DBManger.getInstance(getActivity());
                                manger.delAdvancedCustomInfo();
                                Constants.advCustomInfo = null;
                                Constants.checkAdvCustomInfo(getActivity());
                            }
                        }).show();
                return false;
            }
        });

        mSchoolPreference = findPreference(getString(R.string.pref_school_name));
        mSchoolPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mCustomEnablePreference.isChecked()) {
                    Toast.makeText(getActivity(), "如需选择默认支持的学校请先停用基础自定义", Toast.LENGTH_LONG).show();
                } else {
                    startActivityForResult(new Intent(getActivity(), SchoolSelectionActivity.class),
                            SchoolSelectionActivity.REQUEST_SCHOOL_NAME);
                }
                return true;
            }
        });
        mPreferences.add(mSchoolPreference);

        mCurrentWeekPreference = findPreference(getString(R.string.pref_current_week));
        mPreferences.add(mCurrentWeekPreference);

        mCmsPasswordPreference = findPreference(getString(R.string.pref_cms_password));
        mPreferences.add(mCmsPasswordPreference);

        mLibPasswordPreference = findPreference(getString(R.string.pref_lib_password));
        mPreferences.add(mLibPasswordPreference);

        mCustomEnablePreference = (SwitchPreference) findPreference(getString(R.string.pref_custom_enable));
        mCustomEnablePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mCustomEnablePreference.isChecked()) {
                    bindSummary(mSchoolPreference, PrefHelper.getString(getActivity(), mCustomSchoolNamePreference.getKey(), ""));
                } else {
                    bindSummary(mSchoolPreference, PrefHelper.getString(getActivity(), mSchoolPreference.getKey(), ""));
                }
                return true;
            }
        });
        mPreferences.add(mCmsPasswordPreference);

        mCustomSchoolNamePreference = findPreference(getString(R.string.pref_custom_school_name));
        mPreferences.add(mCustomSchoolNamePreference);

        mPreferences.add(findPreference(getString(R.string.pref_cms_username)));
        mPreferences.add(findPreference(getString(R.string.pref_lib_username)));
        mPreferences.add(findPreference(getString(R.string.pref_custom_cms_sys)));
        mPreferences.add(findPreference(getString(R.string.pref_custom_cms_url)));
        mPreferences.add(findPreference(getString(R.string.pref_custom_lib_sys)));
        mPreferences.add(findPreference(getString(R.string.pref_custom_lib_url)));
        mPreferences.add(findPreference(getString(R.string.pref_homepage_selection)));
        mPreferences.add(findPreference(getString(R.string.pref_empty_class_motto)));
        mPreferences.add(findPreference(getString(R.string.pref_class_name_re)));
        mPreferences.add(findPreference(getString(R.string.pref_class_type_re)));
        mPreferences.add(findPreference(getString(R.string.pref_class_time_re)));
        mPreferences.add(findPreference(getString(R.string.pref_class_during_re)));
        mPreferences.add(findPreference(getString(R.string.pref_class_teacher_re)));
        mPreferences.add(findPreference(getString(R.string.pref_class_place_re)));
        mPreferences.add(findPreference(getString(R.string.pref_every_class_time)));
        mPreferences.add(findPreference(getString(R.string.pref_rest_time)));
    }

    private void bindListener() {
        for (Preference preference : mPreferences) {
            preference.setOnPreferenceChangeListener(this);
            String value = preference.getSharedPreferences().getString(preference.getKey(), "");
            bindSummary(preference, value);
        }
    }

    private void addTimePreferences() {
        int count = Integer.parseInt(PrefHelper.getString(getActivity(), R.string.pref_daily_class_count, "12"));
        for (Preference preference : mTimePreferences) {
            mClassSettingScreen.removePreference(preference);
            mPreferences.remove(preference);
        }
        mTimePreferences.clear();

        for (int i = 0; i < count; i++) {
            Preference preference = new Preference(getActivity());
            final String key = Constants.TIME_PREFIX + i;
            String prefix = "";
            if (8 + i < 10) prefix += "0";
            int t = (8 + i) > 23 ? 23 : (8 + i);
            final String defaultValue = prefix + t + ":00";
            preference.setKey(key);
            preference.setTitle("第 " + (i + 1) + " 节");
            preference.setDefaultValue(defaultValue);
            String value = PrefHelper.getString(getActivity(), key, defaultValue);
            preference.setSummary(value);

            mPreferences.add(preference);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    final String value = PrefHelper.getString(getActivity(), key, defaultValue);
                    final int[] parts = REHelper.getUserSetTime(value);
                    TimePickerDialog dialog = TimePickerDialog.newInstance(new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePickerDialog view, int hourOfDay, int minute, int second) {
                            String value = "";
                            if (hourOfDay < 10) {
                                value += "0";
                            }
                            value += hourOfDay + ":";
                            if (minute < 10) {
                                value += "0";
                            }
                            value += minute;
                            PrefHelper.putString(getActivity(), key, value);
                            preference.setSummary(value);
                        }
                    }, parts[0], parts[1], 0, true);
                    dialog.show(getFragmentManager(), "time_picker");
                    return true;
                }
            });
            mClassSettingScreen.addPreference(preference);
            mPreferences.add(preference);
            mTimePreferences.add(preference);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = newValue.toString();
        bindSummary(preference, value);

        if (preference.equals(mCurrentWeekPreference)) {
            DailyClassWidget.update(getActivity());
        } else if (preference.equals(mCustomSchoolNamePreference)) {
            if (mCustomEnablePreference.isChecked()) {
                bindSummary(mSchoolPreference, value);
            }
        } else if (preference.equals(mCmsPasswordPreference)) {
            return encryption(preference, value, R.string.pref_cms_password, R.string.pref_cms_password_encrypted);
        } else if (preference.equals(mLibPasswordPreference)) {
            return encryption(preference, value, R.string.pref_lib_password, R.string.pref_lib_password_encrypted);
        } else if (preference.equals(mDailyClassCountPreference)) {
            PrefHelper.putString(getActivity(), R.string.pref_daily_class_count, value);
            addTimePreferences();
            return false;
        }
        return true;
    }

    private boolean encryption(Preference preference, String password, int passwordId, int encryptedId) {
        SharedPreferences pref = preference.getSharedPreferences();
        boolean needEncrypt = pref.getBoolean(getString(R.string.pref_need_encryption), true);
        SharedPreferences.Editor editor = pref.edit();
        if (needEncrypt) {
            if (!TextUtils.isEmpty(password)) {
                try {
                    password = AESCrypt.encrypt(Constants.seed, password);
                    editor.putString(getString(passwordId), password);
                    editor.putBoolean(getString(encryptedId), true);
                    preference.setSummary(R.string.encrypted);
                    return false;
                } catch (Exception e) {
                    preference.setSummary(R.string.encrypt_fail);
                    e.printStackTrace();
                } finally {
                    editor.apply();
                }
            }
        } else {
            editor.putBoolean(getString(encryptedId), false);
            preference.setSummary(R.string.unencrypted);
            return true;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                String schoolName = data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT);
                bindSummary(mSchoolPreference, schoolName);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void bindSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            ListPreference listPref = (ListPreference) preference;
            int index = listPref.findIndexOfValue(value);
            listPref.setSummary(index >= 0 ? listPref.getEntries()[index] : null);
        } else if (!(preference instanceof CheckBoxPreference)) {
            if (preference.equals(mSchoolPreference)) {
                SharedPreferences pref = preference.getSharedPreferences();
                if (pref.getBoolean(getString(R.string.pref_custom_enable), false)) {
                    preference.setSummary(pref.getString(getString(R.string.pref_custom_school_name), ""));
                } else {
                    preference.setSummary(value);
                }
                Loader.needUpdateUniversity();
            } else {
                preference.setSummary(value);
            }
        }
    }
}
