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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import cc.metapro.openct.splash.views.LoginFragment;
import cc.metapro.openct.splash.views.SchoolFragment;
import cc.metapro.openct.utils.Constants;


class InitPagerAdapter extends FragmentStatePagerAdapter {

    private SchoolFragment mSchoolFragment;
    private LoginFragment mCmsFragment;
    private LoginFragment mLibFragment;

    InitPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mSchoolFragment = SchoolFragment.getInstance();
        mCmsFragment = LoginFragment.getInstance(Constants.TYPE_CMS);
        mLibFragment = LoginFragment.getInstance(Constants.TYPE_LIB);
        new SplashPresenter(context, mSchoolFragment, mCmsFragment, mLibFragment);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mSchoolFragment;
            case 1:
                return mCmsFragment;
            case 2:
                return mLibFragment;
        }
        throw new IndexOutOfBoundsException("three fragments at most!");
    }

    @Override
    public int getCount() {
        return 3;
    }
}
