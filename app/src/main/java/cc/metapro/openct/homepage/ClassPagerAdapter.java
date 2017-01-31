package cc.metapro.openct.homepage;

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
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.utils.RecyclerViewHelper;


public class ClassPagerAdapter extends PagerAdapter {

    private String[] mTitles = {"今日课表", "本周课表", "学期课表"};
    private List<View> mViewList;
    private Context mContext;
    private DailyClassAdapter mDailyClassAdapter;
    private RecyclerView mRecyclerView;
    private TextView mEmptyView;

    public ClassPagerAdapter(ViewPager viewPager) {
        mViewList = new ArrayList<>(3);
        mContext = viewPager.getContext();
        initViews(viewPager);
        viewPager.setAdapter(this);
    }

    private void initViews(ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        View td = layoutInflater.inflate(R.layout.viewpager_class_today, parent, false);
        mRecyclerView = (RecyclerView) td.findViewById(R.id.class_today_recycler_view);
        mEmptyView = (TextView) td.findViewById(R.id.empty_view);
        mDailyClassAdapter = new DailyClassAdapter(mContext);

        RecyclerViewHelper.setRecyclerView(mContext, mRecyclerView, mDailyClassAdapter);
        mViewList.add(td);

        View tw = layoutInflater.inflate(R.layout.viewpager_class_current_week, parent, false);
        mViewList.add(tw);

        View ts = layoutInflater.inflate(R.layout.viewpager_class_current_sem, parent, false);
        mViewList.add(ts);
    }

    void dismissRecyclerView(String text) {
        mRecyclerView.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        mEmptyView.setText(text);
    }

    void showRecyclerView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mEmptyView.setVisibility(View.GONE);
    }

    DailyClassAdapter getDailyClassAdapter() {
        return mDailyClassAdapter;
    }

    View getWeekClassView() {
        return mViewList.get(1);
    }

    View getSemClassView() {
        return mViewList.get(2);
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(mViewList.get(position));
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles[position];
    }

    void setWeekTitle(int week) {
        mTitles[1] = "本周 (第" + week + "周)";
    }
}
