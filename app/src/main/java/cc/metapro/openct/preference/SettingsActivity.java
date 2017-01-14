package cc.metapro.openct.preference;

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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

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
        ab.setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(R.id.pref_container, new SchoolPreferenceFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        ActivityUtils.encryptionCheck(this);
        Loader.loadUniversity(SettingsActivity.this);
        super.onDestroy();
    }

    public static class SchoolPreferenceFragment extends PreferenceFragment {

        public Preference.OnPreferenceChangeListener
                sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                String stringValue = value.toString();

                if (preference instanceof ListPreference) {
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);
                    preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
                } else {
                    preference.setSummary(stringValue);
                }

                if (preference.getKey().equals(Constants.PREF_CURRENT_WEEK_KEY)) {
                    DailyClassWidget.update(preference.getContext());
                } else if (preference.getKey().equals(Constants.PREF_CMS_PASSWORD_KEY)) {
                    String prev = preference.getSharedPreferences()
                            .getString(Constants.PREF_CMS_PASSWORD_KEY, stringValue);

                    if (!stringValue.equals(prev)) {
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(Constants.PREF_CMS_PASSWORD_ENCRYPTED, false);
                        editor.apply();
                    }
                } else if (preference.getKey().equals(Constants.PREF_LIB_PASSWORD_KEY)) {
                    String prev = preference.getSharedPreferences()
                            .getString(Constants.PREF_LIB_PASSWORD_KEY, stringValue);

                    if (!stringValue.equals(prev)) {
                        SharedPreferences.Editor editor = preference.getEditor();
                        editor.putBoolean(Constants.PREF_LIB_PASSWORD_ENCRYPTED, false);
                        editor.apply();
                    }
                }
                return true;
            }
        };

        public SchoolPreferenceFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_school);
            setHasOptionsMenu(false);

            bindSummaryToValue(findPreference(Constants.PREF_SCHOOL_ABBR_KEY));
            bindSummaryToValue(findPreference(Constants.PREF_CURRENT_WEEK_KEY));
            bindSummaryToValue(findPreference(Constants.PREF_CMS_USERNAME_KEY));
            bindSummaryToValue(findPreference(Constants.PREF_LIB_USERNAME_KEY));

            passwordOperation(findPreference(Constants.PREF_CMS_PASSWORD_KEY));
            passwordOperation(findPreference(Constants.PREF_LIB_PASSWORD_KEY));
        }

        private void bindSummaryToValue(Preference preference) {
            if (preference == null) return;
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }

        private void passwordOperation(Preference preference) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
        }
    }

}
