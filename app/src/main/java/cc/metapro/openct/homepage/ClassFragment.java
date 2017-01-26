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

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.RecyclerViewHelper;

@Keep
public class ClassFragment extends Fragment implements ClassContract.View {

    private static boolean showedPrompt;

    @BindView(R.id.class_view_pager)
    ViewPager mViewPager;

    private TextView mEmptyView;
    private RecyclerView todayRecyclerView;
    private ClassContract.Presenter mPresenter;
    private FragmentActivity mContext;
    private List<View> mViewList;
    private Map<String, View> mViewMap;
    private DailyClassAdapter mTodayClassAdapter;
    private String[] mTitles = {"今日课表", "本周课表", "学期课表"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class, container, false);
        ButterKnife.bind(this, view);
        mContext = getActivity();
        initViewPager(view);
        return view;
    }

    @Override
    public void onResume() {
        mPresenter.start();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initViewPager(View view) {
        mViewList = new ArrayList<>();
        mViewMap = new HashMap<>(3);

        PagerTabStrip strip = (PagerTabStrip) view.findViewById(R.id.class_view_pager_title);
        strip.setTextColor(Color.WHITE);
        strip.setTabIndicatorColor(ContextCompat.getColor(mContext, R.color.colorAccent));
        LayoutInflater layoutInflater = getLayoutInflater(getArguments());

        View td = layoutInflater.inflate(R.layout.viewpager_class_today, mViewPager, false);
        todayRecyclerView = (RecyclerView) td.findViewById(R.id.class_today_recycler_view);
        mEmptyView = (TextView) td.findViewById(R.id.empty_view);
        mTodayClassAdapter = new DailyClassAdapter(mContext);
        RecyclerViewHelper.setRecyclerView(mContext, todayRecyclerView, mTodayClassAdapter);
        mViewList.add(td);
        mViewMap.put("td", td);

        View tw = layoutInflater.inflate(R.layout.viewpager_class_current_week, mViewPager, false);
        mViewList.add(tw);
        mViewMap.put("tw", tw);

        View ts = layoutInflater.inflate(R.layout.viewpager_class_current_sem, mViewPager, false);
        mViewList.add(ts);
        mViewMap.put("ts", ts);

        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mTitles.length;
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
        });
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String homeIndex = preferences.getString(getString(R.string.pref_homepage_selection), "0");
        int index = Integer.parseInt(homeIndex);
        mViewPager.setCurrentItem(index);
    }

    private void addSeqViews(ViewGroup index) {
        if (index != null) {
            index.removeAllViews();
            for (int i = 1; i <= Constants.DAILY_CLASSES; i++) {
                TextView textView = new TextView(mContext);
                textView.setText("第\n" + i + "\n节");
                textView.setGravity(Gravity.CENTER);
                textView.setMinHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
                textView.setMaxHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
                textView.setTextSize(10);
                index.addView(textView);
            }
        }
    }

    private void addContentView(ViewGroup content, List<EnrichedClassInfo> classes, int thisWeek) {
        if (content == null) return;
        content.removeAllViews();
        for (EnrichedClassInfo info : classes) {
            // 仅显示本周
            info.addViewTo(content, mContext, mPresenter, thisWeek);
        }
    }

    @Override
    public void updateClasses(List<EnrichedClassInfo> classes) {
        int week = Loader.getCurrentWeek(mContext);
        mTitles[1] = "本周 (第" + week + "周)";

        mTodayClassAdapter.updateTodayClasses(classes, week);
        mTodayClassAdapter.notifyDataSetChanged();

        View view = mViewMap.get("ts");
        ViewGroup seq = (ViewGroup) view.findViewById(R.id.sem_class_seq);
        ViewGroup con = (ViewGroup) view.findViewById(R.id.sem_class_content);
        addSeqViews(seq);
        addContentView(con, classes, -1);
        showSelectedWeek(classes, week);

        if (mTodayClassAdapter.hasClassToday()) {
            int count = mTodayClassAdapter.getItemCount();
            mEmptyView.setVisibility(View.GONE);
            todayRecyclerView.setVisibility(View.VISIBLE);
            if (!showedPrompt) {
                showedPrompt = true;
                Snackbar.make(mViewPager, "今天有 " + count + " 节课", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            String motto = preferences.getString(getString(R.string.pref_empty_class_motto), getString(R.string.default_motto));
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setText(motto);
            todayRecyclerView.setVisibility(View.GONE);
            if (!showedPrompt) {
                showedPrompt = true;
                Snackbar.make(mViewPager, "今天没有课, 好好休息一下吧~", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void showFormDialog(Form form) {
        FormDialog.newInstance(form, mPresenter).show(getFragmentManager(), "form_dialog");
    }

    private void showSelectedWeek(List<EnrichedClassInfo> classes, int week) {
        View view = mViewMap.get("tw");
        ViewGroup seq = (ViewGroup) view.findViewById(R.id.week_class_seq);
        ViewGroup con = (ViewGroup) view.findViewById(R.id.week_class_content);
        addSeqViews(seq);
        addContentView(con, classes, week);
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
    }

}
