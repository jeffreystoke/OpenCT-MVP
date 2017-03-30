package cc.metapro.openct.myclass;

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

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.myclass.classviews.DailyFragment;
import cc.metapro.openct.myclass.classviews.TableFragment;

import static cc.metapro.openct.myclass.classviews.TableFragment.TYPE_SEM;
import static cc.metapro.openct.myclass.classviews.TableFragment.TYPE_WEEK;


class ClassPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;

    ClassPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return DailyFragment.newInstance();
            case 1:
                return TableFragment.newInstance(TYPE_WEEK);
            case 2:
                return TableFragment.newInstance(TYPE_SEM);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.text_daily_classes);
            case 1:
                return mContext.getString(R.string.text_current_week, Loader.getCurrentWeek(mContext));
            case 2:
                return mContext.getString(R.string.text_sem_classes);
        }
        return "";
    }

}
