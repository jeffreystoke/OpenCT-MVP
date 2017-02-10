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

import android.app.ActionBar;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.Constants;

@Keep
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        ButterKnife.bind(this);

        // setup actionbar
        ActionBar ab = getActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.pref_container, new SchoolPreferenceFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        storeCustom();
        super.onDestroy();
    }

    private void storeCustom() {
        DBManger manger = DBManger.getInstance(this);
        UniversityInfo info = new UniversityInfo();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        info.cmsSys = pref.getString(getString(R.string.pref_custom_cms_sys), Constants.COMMON);
        info.cmsURL = pref.getString(getString(R.string.pref_custom_cms_url), "");
        info.libSys = pref.getString(getString(R.string.pref_custom_lib_sys), Constants.NJHUIWEN);
        info.libURL = pref.getString(getString(R.string.pref_custom_lib_url), "");
        manger.updateCustomSchoolInfo(info);
    }
}
