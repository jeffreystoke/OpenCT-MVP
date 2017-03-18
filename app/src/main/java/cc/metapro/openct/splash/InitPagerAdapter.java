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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import cc.metapro.openct.splash.views.CmsFragment;
import cc.metapro.openct.splash.views.LibraryFragment;
import cc.metapro.openct.splash.views.SchoolFragment;


class InitPagerAdapter extends FragmentStatePagerAdapter {

    InitPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return SchoolFragment.getInstance();
            case 1:
                return CmsFragment.getInstance();
            case 2:
                return LibraryFragment.getInstance();
        }
        throw new IndexOutOfBoundsException("three fragments at most!");
    }

    @Override
    public int getCount() {
        return 3;
    }
}
