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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


class InitPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private List<InitPagerFragment> mFragments;
    private int[] TYPES = {InitPagerFragment.TYPE_SCHOOL_INFO, InitPagerFragment.TYPE_CMS_INFO, InitPagerFragment.TYPE_LIB_INFO};

    InitPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = new ArrayList<>();
        setFragments();
    }

    private void setFragments() {
        for (int TYPE : TYPES) {
            Bundle bundle = new Bundle();
            bundle.putInt(InitPagerFragment.TYPE_KEY, TYPE);
            InitPagerFragment fragment = new InitPagerFragment();
            fragment.setArguments(bundle);
            fragment.setContext(mContext);
            mFragments.add(fragment);
        }
    }

    void storeSettings() {
        for (InitPagerFragment fragment : mFragments) {
            fragment.storeInfo();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
