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
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.splash.schoolselection.SchoolSelectionActivity;
import cc.metapro.openct.utils.PrefHelper;

/**
 * A simple {@link Fragment} subclass.
 */
public class InitPagerFragment extends Fragment {

    public static final int TYPE_SCHOOL_INFO = 0;
    public static final int TYPE_CMS_INFO = 1;
    public static final int TYPE_LIB_INFO = 2;

    public static final String TYPE_KEY = "type";

    @BindView(R.id.header_image)
    ImageView mImageView;

    @BindView(R.id.school_info)
    ViewGroup mSchoolInfoView;

    @BindView(R.id.cms_info)
    ViewGroup mCmsInfoView;

    @BindView(R.id.lib_info)
    ViewGroup mLibInfoView;

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
    private int TYPE;
    private Context mContext;

    @OnClick(R.id.info_init_school)
    public void startSelection() {
        startActivityForResult(new Intent(getActivity(), SchoolSelectionActivity.class),
                SchoolSelectionActivity.REQUEST_SCHOOL_NAME);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_init_pager, container, false);
        ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        TYPE = bundle.getInt(TYPE_KEY);
        switch (TYPE) {
            case TYPE_SCHOOL_INFO:
                mImageView.setBackgroundResource(R.drawable.ic_school_header);
                mSchoolInfoView.setVisibility(View.VISIBLE);
                break;
            case TYPE_CMS_INFO:
                mImageView.setBackgroundResource(R.drawable.ic_cms_header);
                mCmsInfoView.setVisibility(View.VISIBLE);
                break;
            case TYPE_LIB_INFO:
                mImageView.setBackgroundResource(R.drawable.ic_lib_header);
                mLibInfoView.setVisibility(View.VISIBLE);
                break;
        }
        return view;
    }

    void setContext(Context context) {
        mContext = context;
    }

    void storeInfo() {
        switch (TYPE) {
            case TYPE_SCHOOL_INFO:
                int i = weekSpinner.getSelectedItemPosition();
                String week = mContext.getResources().getStringArray(R.array.pref_week_seq_values)[i];
                PrefHelper.putString(mContext, R.string.pref_current_week, week);
                PrefHelper.putInt(mContext, R.string.pref_week_set_week, Calendar.getInstance().get(Calendar.WEEK_OF_YEAR));
                PrefHelper.putBoolean(mContext, R.string.pref_need_encryption, true);
                break;
            case TYPE_CMS_INFO:
                PrefHelper.putString(mContext, R.string.pref_cms_username, cmsUsername.getText().toString());
                PrefHelper.putString(mContext, R.string.pref_cms_password, cmsPassword.getText().toString());
                break;
            case TYPE_LIB_INFO:
                PrefHelper.putString(mContext, R.string.pref_lib_username, libUsername.getText().toString());
                PrefHelper.putString(mContext, R.string.pref_lib_password, libPassword.getText().toString());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                schoolText.setText(data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
