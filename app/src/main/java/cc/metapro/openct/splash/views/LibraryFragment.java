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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.scottyab.aescrypt.AESCrypt;

import java.security.GeneralSecurityException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

public class LibraryFragment extends Fragment {

    @BindView(R.id.img)
    ImageView mImageView;

    @BindView(R.id.username)
    EditText mUsername;

    @BindView(R.id.password)
    EditText mPassword;

    private boolean showed = false;

    public static LibraryFragment getInstance() {
        return new LibraryFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_userpass, container, false);
        ButterKnife.bind(this, view);
        mImageView.setImageResource(R.drawable.ic_lib_header);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {
            showed = true;
        } else if (showed) {
            String username = "";
            String password = "";
            try {
                username = mUsername.getText().toString();
                password = AESCrypt.encrypt(Constants.seed, mPassword.getText().toString());
            } catch (GeneralSecurityException e) {
                Log.e("FATAL:ENCRYPTION FAIL", e.getMessage());
            } finally {
                PrefHelper.putString(getContext(), R.string.pref_lib_username, username);
                PrefHelper.putString(getContext(), R.string.pref_lib_password, password);
            }
        }
        super.setUserVisibleHint(isVisibleToUser);
    }
}
