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

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

public class CustomActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private ProfileEditFragment mProfileFragment;

    private CustomContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        mProfileFragment =
                (ProfileEditFragment) fm.findFragmentById(R.id.custom_fragment_container);

        if (mProfileFragment == null) {
            mProfileFragment = new ProfileEditFragment();
            ActivityUtils.addFragmentToActivity(fm, mProfileFragment, R.id.custom_fragment_container);
        }
        mPresenter = new CustomPresenter(this, mProfileFragment);
    }
}
