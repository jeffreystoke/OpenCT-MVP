package cc.metapro.openct.customviews;

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
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Spinner;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.homepage.schoolselection.SchoolSelectionActivity;
import cc.metapro.openct.utils.ActivityUtils;

@Keep
public class InitDialog extends DialogFragment {

    @BindView(R.id.info_init_cms_username)
    MaterialEditText cmsUsername;
    @BindView(R.id.info_init_cms_password)
    MaterialEditText cmsPassword;
    @BindView(R.id.info_init_lib_username)
    MaterialEditText libUsername;
    @BindView(R.id.info_init_lib_password)
    MaterialEditText libPassword;
    @BindView(R.id.info_init_school)
    TextView schoolText;
    @BindView(R.id.info_init_week)
    Spinner weekSpinner;
    private String schoolName;

    public static InitDialog newInstance() {
        InitDialog initDialog = new InitDialog();
        initDialog.setCancelable(false);
        return new InitDialog();
    }

    @OnClick(R.id.info_init_school)
    public void startSelection() {
        startActivityForResult(new Intent(getActivity(), SchoolSelectionActivity.class),
                SchoolSelectionActivity.REQUEST_SCHOOL_NAME);
    }

    @OnClick(R.id.ok)
    public void storePref() {
        int i = weekSpinner.getSelectedItemPosition();
        String week = getResources().getStringArray(R.array.pref_week_seq_values)[i];
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = p.edit();
        editor.putString(getString(R.string.pref_school_name), schoolName);
        editor.putString(getString(R.string.pref_current_week), week);
        editor.putInt(getString(R.string.pref_week_set_week), Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
        editor.putString(getString(R.string.pref_cms_username), cmsUsername.getText().toString());
        editor.putString(getString(R.string.pref_cms_password), cmsPassword.getText().toString());
        editor.putString(getString(R.string.pref_lib_username), libUsername.getText().toString());
        editor.putString(getString(R.string.pref_lib_password), libPassword.getText().toString());
        editor.putBoolean(getString(R.string.pref_init), true);
        editor.putBoolean(getString(R.string.pref_need_encryption), true);
        editor.apply();
        ActivityUtils.encryptionCheck(getActivity());
        Loader.needUpdateUniversity();
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.dialog_init, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                schoolName = data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT);
                schoolText.setText(schoolName);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
