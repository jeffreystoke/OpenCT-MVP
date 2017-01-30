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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import com.scottyab.aescrypt.AESCrypt;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.homepage.schoolselection.SchoolSelectionActivity;
import cc.metapro.openct.utils.Constants;
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
        } else if (preference.equals(mCmsPasswordPreference) || preference.equals(mLibPasswordPreference)) {
            SharedPreferences pref = preference.getSharedPreferences();
            boolean needEncrypt = pref.getBoolean(getString(R.string.pref_need_encryption), true);
            if (needEncrypt) {
                if (!TextUtils.isEmpty(value)) {
                    SharedPreferences.Editor editor = pref.edit();
                    try {
                        value = AESCrypt.encrypt(Constants.seed, value);
                        editor.putString(getString(R.string.pref_cms_password), value);
                        editor.putBoolean(getString(R.string.pref_cms_password_encrypted), true);
                        preference.setSummary("已加密");
                    } catch (Exception e) {
                        preference.setSummary("加密失败, 请关闭加密选项");
                        e.printStackTrace();
                    } finally {
                        editor.apply();
                    }
                }
            } else {
                preference.setSummary("未加密");
            }
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
