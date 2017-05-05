package cc.metapro.openct.splash;

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
import android.text.TextUtils;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.Locale;

import cc.metapro.openct.R;
import cc.metapro.openct.splash.views.SplashContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;


class SplashPresenter implements SplashContract.Presenter {

    private Context mContext;

    private SplashContract.SchoolView mSchoolView;

    SplashPresenter(Context context,
                    SplashContract.SchoolView schoolView,
                    SplashContract.LoginView cmsView,
                    SplashContract.LoginView libView) {
        mContext = context;
        mSchoolView = schoolView;
        schoolView.setPresenter(this);
        cmsView.setPresenter(this);
        libView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void setSelectedSchool(String name) {
        PrefHelper.putString(mContext, R.string.pref_school_name, name);
        mSchoolView.showSelectedSchool(name);
    }

    @Override
    public void setSelectedWeek(int week) {
        int currentWeekOfYear = Calendar.getInstance(Locale.CHINA)
                .get(Calendar.WEEK_OF_YEAR);
        PrefHelper.putString(mContext, R.string.pref_current_week, week + "");
        PrefHelper.putString(mContext, R.string.pref_week_set_week, currentWeekOfYear + "");
    }

    @Override
    public void storeCMSUserPass(String username, String password) {
        try {
            password = getEncryptedPassword(password);
        } catch (Exception ignored) {

        } finally {
            PrefHelper.putString(mContext, R.string.pref_cms_username, username);
            PrefHelper.putString(mContext, R.string.pref_cms_password, password);
        }
    }

    @Override
    public void storeLibUserPass(String username, String password) {
        try {
            password = getEncryptedPassword(password);
        } catch (Exception ignored) {

        } finally {
            PrefHelper.putString(mContext, R.string.pref_lib_username, username);
            PrefHelper.putString(mContext, R.string.pref_lib_password, password);
        }
    }

    private String getEncryptedPassword(String password) throws GeneralSecurityException {
        if (TextUtils.isEmpty(password))
            throw new NullPointerException("password shouldn't be empty");
        return AESCrypt.encrypt(Constants.seed, password);
    }
}
