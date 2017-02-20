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

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.myclass.schoolselection.SchoolSelectionActivity;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.widget.DailyClassWidget;

public class SchoolPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private Preference mSchoolPreference;
    private Preference mCurrentWeekPreference;
    private Preference mCmsPasswordPreference;
    private Preference mLibPasswordPreference;
    private CheckBoxPreference mCustomEnablePreference;
    private Preference mCustomSchoolNamePreference;
    private List<Preference> mPreferences;

    public SchoolPreferenceFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_school);
        setHasOptionsMenu(false);
        ButterKnife.bind(this, getActivity());
        mPreferences = new ArrayList<>();

        PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.pref_class_settings));
        int count = Integer.parseInt(PrefHelper.getString(getActivity(), R.string.pref_daily_class_count, "12"));
        for (int i = 0; i < count; i++) {
            Preference preference = new Preference(getActivity());
            preference.setKey("class_time_" + i);
            preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    // TODO: 17/2/20 add datePickerDialog here
                    return false;
                }
            });
            screen.addPreference(preference);
            mPreferences.add(preference);
        }

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
                                manger.delAdvancedCustomClassInfo();
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

        mCustomEnablePreference = (CheckBoxPreference) findPreference(getString(R.string.pref_custom_enable));
        mCustomEnablePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (mCustomEnablePreference.isChecked()) {
                    bindSummary(mSchoolPreference, preference.getSharedPreferences().getString(mCustomSchoolNamePreference.getKey(), ""));
                } else {
                    bindSummary(mSchoolPreference, preference.getSharedPreferences().getString(mSchoolPreference.getKey(), ""));
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
        bindListener();
    }

    private void bindListener() {
        for (Preference preference : mPreferences) {
            preference.setOnPreferenceChangeListener(this);
            String value = preference.getSharedPreferences().getString(preference.getKey(), "");
            bindSummary(preference, value);
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
