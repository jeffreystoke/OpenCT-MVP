package cc.metapro.openct.custom;

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
import android.widget.Toast;

import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.Constants;

public class CustomPresenter implements CustomContract.Presenter {

    private Context mContext;

    private CustomContract.View mView;

    public CustomPresenter(Context context, CustomContract.View view) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void storeProfile(boolean enable) {
        try {
            UniversityInfo.SchoolInfo schoolInfo = mView.getCustomFactory();
            DBManger manger = DBManger.getInstance(mContext);
            if (!manger.updateCustomSchoolInfo(schoolInfo)) {
                Toast.makeText(mContext, "保存失败: 需要唯一的简拼!", Toast.LENGTH_LONG).show();
            }
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = preferences.edit();
            if (enable) {
                editor.putBoolean(Constants.PREF_USE_CUSTOM, true);
            } else {
                editor.putBoolean(Constants.PREF_USE_CUSTOM, false);
            }
            editor.apply();
            Loader.loadUniversity(mContext);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void loadProfile() {
        DBManger manger = DBManger.getInstance(mContext);
        UniversityInfo.SchoolInfo info = manger.getCustomSchoolInfo();
        if (info != null) {
            mView.showProfile(info);
        }
    }

    @Override
    public void start() {
        loadProfile();
    }
}
