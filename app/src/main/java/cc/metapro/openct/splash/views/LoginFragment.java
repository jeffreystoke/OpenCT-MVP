package cc.metapro.openct.splash.views;


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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;

import static cc.metapro.openct.utils.Constants.TYPE_CMS;
import static cc.metapro.openct.utils.Constants.TYPE_LIB;

public class LoginFragment extends Fragment implements SplashContract.LoginView {
    private static final String KEY_TYPE = "type";

    @BindView(R.id.img)
    ImageView mImageView;
    @BindView(R.id.username)
    EditText mUsername;
    @BindView(R.id.password)
    EditText mPassword;

    private SplashContract.Presenter mPresenter;
    private int mType = TYPE_CMS;
    private boolean showed = false;

    public static LoginFragment getInstance(int type) {
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_TYPE, type);

        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userpass, container, false);
        ButterKnife.bind(this, view);
        mType = getArguments().getInt(KEY_TYPE);
        if (mType == TYPE_LIB) {
            mImageView.setImageResource(R.drawable.ic_lib_header);
        } else {
            mImageView.setImageResource(R.drawable.ic_cms_header);
        }
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            showed = true;
        } else if (showed) {
            String username = mUsername.getText().toString();
            String password = mPassword.getText().toString();
            if (mType == TYPE_CMS) {
                mPresenter.storeCMSUserPass(username, password);
            } else if (mType == TYPE_LIB) {
                mPresenter.storeLibUserPass(username, password);
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
