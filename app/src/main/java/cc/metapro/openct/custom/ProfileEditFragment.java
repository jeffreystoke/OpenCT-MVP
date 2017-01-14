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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.utils.Constants;

public class ProfileEditFragment extends Fragment implements CustomContract.View {
    @BindView(R.id.enable)
    CheckBox enable;
    @BindView(R.id.school_name)
    EditText name;
    @BindView(R.id.school_abbr)
    EditText abbr;
    @BindView(R.id.cms_sys)
    Spinner cmsSys;
    @BindView(R.id.cms_addr)
    EditText cmsAddr;
    @BindView(R.id.cms_dyn_url)
    CheckBox cmsDynUrl;
    @BindView(R.id.cms_captcha)
    CheckBox cmsCaptcha;
    @BindView(R.id.cms_inner)
    CheckBox cmsInner;
    @BindView(R.id.lib_sys)
    Spinner libSys;
    @BindView(R.id.lib_addr)
    EditText libAddr;
    @BindView(R.id.lib_dyn_url)
    CheckBox libDynUrl;
    @BindView(R.id.lib_captcha)
    CheckBox libCaptcha;
    @BindView(R.id.lib_inner)
    CheckBox libInner;

    private CustomContract.Presenter mPresenter;

    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mContext = getContext();
        mPresenter.start();
    }

    @Override
    public void onDestroy() {
        mPresenter.storeProfile(enable.isChecked());
        super.onDestroy();
    }

    @Override
    public UniversityInfo.SchoolInfo getCustomFactory() {
        UniversityInfo.SchoolInfo info = new UniversityInfo.SchoolInfo();

        info.name = name.getText().toString();
        info.abbr = abbr.getText().toString();

        // cms info
        String[] cmses = mContext.getResources().getStringArray(R.array.school_cms_values);
        info.cmsSys = cmses[cmsSys.getSelectedItemPosition()];
        info.cmsURL = cmsAddr.getText().toString();
        info.cmsInnerAccess = cmsInner.isChecked();
        info.cmsCaptcha = cmsCaptcha.isChecked();
        info.cmsDynURL = cmsDynUrl.isChecked();

        // lib info
        String[] libs = mContext.getResources().getStringArray(R.array.school_lib_values);
        info.libSys = libs[libSys.getSelectedItemPosition()];
        info.libURL = libAddr.getText().toString();
        info.libInnerAccess = libInner.isChecked();
        info.libCaptcha = libCaptcha.isChecked();
        info.libDynURL = libDynUrl.isChecked();

        return info;
    }

    @Override
    public void showProfile(UniversityInfo.SchoolInfo info) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (preferences.getBoolean(Constants.PREF_USE_CUSTOM, false)) {
            enable.setChecked(true);
        }
        name.setText(info.name);
        abbr.setText(info.abbr);
        String[] cmses = mContext.getResources().getStringArray(R.array.school_cms_values);
        for (int i = 0; i < cmses.length; i++) {
            if (cmses[i].equalsIgnoreCase(info.cmsSys)) {
                cmsSys.setSelection(i);
            }
        }
        String[] libs = mContext.getResources().getStringArray(R.array.school_lib_values);
        for (int i = 0; i < libs.length; i++) {
            if (libs[i].equalsIgnoreCase(info.cmsSys)) {
                cmsSys.setSelection(i);
            }
        }
        cmsAddr.setText(info.cmsURL);
        libAddr.setText(info.libURL);
        if (info.cmsCaptcha) {
            cmsCaptcha.setChecked(true);
        }
        if (info.cmsDynURL) {
            cmsDynUrl.setChecked(true);
        }
        if (info.cmsInnerAccess) {
            cmsInner.setChecked(true);
        }
        if (info.libCaptcha) {
            libCaptcha.setChecked(true);
        }
        if (info.libDynURL) {
            libDynUrl.setChecked(true);
        }
        if (info.libInnerAccess) {
            libInner.setChecked(true);
        }
    }

    @Override
    public void setPresenter(CustomContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
