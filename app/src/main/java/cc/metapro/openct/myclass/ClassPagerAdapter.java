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
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.myclass.classviews.DailyFragment;
import cc.metapro.openct.myclass.classviews.TableFragment;
import cc.metapro.openct.utils.Constants;


class ClassPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private String titleWeek;
    private DailyFragment mDailyFragment;
    private TableFragment mTableFragment;

    ClassPagerAdapter(FragmentManager manager, Context context) {
        super(manager);
        mContext = context;
        titleWeek = mContext.getString(R.string.text_current_week, Loader.getCurrentWeek(mContext));
        mDailyFragment = DailyFragment.newInstance();
        mTableFragment = TableFragment.newInstance();
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return mDailyFragment;
            case 1:
                return mTableFragment;
        }
        throw new IndexOutOfBoundsException("two fragments at most");
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.text_daily_classes);
            case 1:
                return titleWeek;
        }
        throw new IndexOutOfBoundsException("two fragments at most");
    }

    public void updateClasses(Classes classes, int week) {
        mDailyFragment.showClasses(classes, week);
        mTableFragment.showClasses(classes, week);
    }

    public void showTableDesignatedWeek(Classes classes, int week) {
        mTableFragment.showClasses(classes, week);
        notifyDataSetChanged();
    }

}
