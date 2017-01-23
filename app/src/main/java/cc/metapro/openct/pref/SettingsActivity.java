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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.widget.DailyClassWidget;

@Keep
public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.pref_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        ButterKnife.bind(this);

        // setup toolbar
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.pref_container, new SchoolPreferenceFragment())
                .commit();
    }

    private void storeCustom() {
        DBManger manger = DBManger.getInstance(this);
        UniversityInfo.SchoolInfo info = new UniversityInfo.SchoolInfo();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        info.cmsSys = pref.getString(getString(R.string.pref_custom_cms_sys), Constants.ZFSOFT2012);
        info.cmsURL = pref.getString(getString(R.string.pref_custom_cms_url), "http://example.com/");
        info.libSys = pref.getString(getString(R.string.pref_custom_lib_sys), Constants.LIBSYS);
        info.libURL = pref.getString(getString(R.string.pref_custom_lib_url), "http://example.com");
        info.cmsDynURL = pref.getBoolean(getString(R.string.pref_custom_cms_dyn_url), false);
        info.cmsCaptcha = pref.getBoolean(getString(R.string.pref_custom_cms_captcha), false);
        info.cmsInnerAccess = pref.getBoolean(getString(R.string.pref_custom_cms_inner), false);
        info.libDynURL = pref.getBoolean(getString(R.string.pref_custom_lib_dyn_url), false);
        info.libCaptcha = pref.getBoolean(getString(R.string.pref_custom_lib_captcha), false);
        info.libInnerAccess = pref.getBoolean(getString(R.string.pref_custom_lib_inner), false);
        manger.updateCustomSchoolInfo(info);
    }

    @Override
    protected void onDestroy() {
        storeCustom();
        ActivityUtils.encryptionCheck(this);
        Loader.loadUniversity(SettingsActivity.this);
        super.onDestroy();
    }

    public static class SchoolPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @BindString(R.string.pref_school_name)
        String SCHOOL_NAME;

        @BindString(R.string.pref_current_week)
        String CURRENT_WEEK;

        @BindString(R.string.pref_cms_username)
        String CMS_USERNAME;

        @BindString(R.string.pref_cms_password)
        String CMS_PASSWORD;

        @BindString(R.string.pref_lib_username)
        String LIB_USERNAME;

        @BindString(R.string.pref_lib_password)
        String LIB_PASSWORD;

        @BindString(R.string.pref_cms_password_encrypted)
        String CMS_PASSWORD_ENCRYPTED;

        @BindString(R.string.pref_lib_password_encrypted)
        String LIB_PASSWORD_ENCRYPTED;

        @BindString(R.string.need_encryption)
        String NEED_ENCRYPTION;

        @BindString(R.string.pref_custom_cms_sys)
        String CUSTOM_CMS_SYS;

        @BindString(R.string.pref_custom_lib_sys)
        String CUSTOM_LIB_SYS;

        @BindString(R.string.pref_custom_cms_url)
        String CUSTOM_CMS_URL;

        @BindString(R.string.pref_custom_lib_url)
        String CUSTOM_LIB_URL;

        @BindString(R.string.pref_custom_enable)
        String CUSTOM_ENABLE;

        @BindString(R.string.pref_custom_school_name)
        String CUSTOM_SCHOOL_NAME;

        public SchoolPreferenceFragment() {

        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_school);
            setHasOptionsMenu(false);
            ButterKnife.bind(this, getActivity());

            bindSummaryToValue(findPreference(SCHOOL_NAME));
            bindSummaryToValue(findPreference(CURRENT_WEEK));
            bindSummaryToValue(findPreference(CMS_USERNAME));
            bindSummaryToValue(findPreference(CMS_PASSWORD));
            bindSummaryToValue(findPreference(LIB_USERNAME));
            bindSummaryToValue(findPreference(LIB_PASSWORD));
            bindSummaryToValue(findPreference(CUSTOM_CMS_SYS));
            bindSummaryToValue(findPreference(CUSTOM_CMS_URL));
            bindSummaryToValue(findPreference(CUSTOM_LIB_SYS));
            bindSummaryToValue(findPreference(CUSTOM_LIB_URL));
            bindSummaryToValue(findPreference(CUSTOM_SCHOOL_NAME));
            bindSummaryToValue(findPreference(CUSTOM_ENABLE));
        }

        private void bindSummaryToValue(Preference preference) {
            if (preference == null) return;
            Context context = preference.getContext();
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            preference.setOnPreferenceChangeListener(this);
            if (CUSTOM_ENABLE.equals(preference.getKey())) {
                onPreferenceChange(preference, pref.getBoolean(CUSTOM_ENABLE, false));
            } else {
                onPreferenceChange(preference, pref.getString(preference.getKey(), ""));
            }
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();

            // 将值设置为 Summary
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }

            SharedPreferences pref = preference.getSharedPreferences();
            String key = preference.getKey();

            if (key.equals(CURRENT_WEEK)) {
                // 更新了周数, 更新课表插件
                DailyClassWidget.update(preference.getContext());
            }

            if (key.equals(CUSTOM_ENABLE)) {
                Preference schoolName = findPreference(SCHOOL_NAME);
                if (schoolName != null) {
                    if (pref.getBoolean(CUSTOM_ENABLE, false)) {
                        schoolName.setSummary(pref.getString(CUSTOM_SCHOOL_NAME, "OPEN CT"));
                        schoolName.setSelectable(false);
                    } else {
                        schoolName.setSummary(pref.getString(SCHOOL_NAME, null));
                        schoolName.setSelectable(true);
                    }
                }
            }

            if (key.equals(CMS_PASSWORD)) {
                String prev = pref.getString(CMS_PASSWORD, stringValue);

                // 更新了教务网密码
                if (!stringValue.equals(prev)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(CMS_PASSWORD_ENCRYPTED, false);
                    editor.apply();
                }
            } else if (key.equals(LIB_PASSWORD)) {
                String prev = pref.getString(LIB_PASSWORD, stringValue);

                // 更新了图书馆密码
                if (!stringValue.equals(prev)) {
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putBoolean(LIB_PASSWORD_ENCRYPTED, false);
                    editor.apply();
                }
            }
            return true;
        }
    }
}
