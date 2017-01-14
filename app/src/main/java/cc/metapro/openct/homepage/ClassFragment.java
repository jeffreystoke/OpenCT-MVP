package cc.metapro.openct.homepage;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class ClassFragment extends Fragment implements ClassContract.View {

    @BindView(R.id.class_view_pager)
    ViewPager mViewPager;

    private ClassContract.Presenter mPresenter;
    private Context mContext;
    private List<View> mViewList;
    private Map<String, View> mViewMap;
    private DailyClassAdapter mTodayClassAdapter;
    private String[] mTitles = {"今日课表", "本周课表", "学期课表"};

    private static boolean showedPrompt;

    private int height, width;
    private int colorIndex;
    private int classLength = Loader.getClassLength();
    private int dailyClasses = Loader.getDailyClasses();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mContext = getContext();
        View mainView = inflater.inflate(R.layout.fragment_class, container, false);

        ButterKnife.bind(this, mainView);

        initViewPager(mainView);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        width = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
        height = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));

        return mainView;
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

        View td = layoutInflater.inflate(R.layout.viewpager_class_today, null);
        RecyclerView todayRecyclerView = (RecyclerView) td.findViewById(R.id.class_today_recycler_view);
        mTodayClassAdapter = new DailyClassAdapter(mContext);
        RecyclerViewHelper.setRecyclerView(mContext, todayRecyclerView, mTodayClassAdapter);
        mViewList.add(td);
        mViewMap.put("td", td);

        View tw = layoutInflater.inflate(R.layout.viewpager_class_current_week, null);
        mViewList.add(tw);
        mViewMap.put("tw", tw);

        View ts = layoutInflater.inflate(R.layout.viewpager_class_current_sem, null);
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
        mViewPager.setCurrentItem(0);
    }

    private void addSeqViews(ViewGroup index) {
        index.removeAllViews();
        for (int i = 1; i <= dailyClasses * classLength; i++) {
            TextView textView = new TextView(mContext);
            if (classLength == 2) {
                if (i % classLength == 0) continue;
                textView.setText("第\n" + i + "\n~\n" + (i + 1) + "\n节");
            } else {
                textView.setText("第\n" + i + "\n节");
            }
            textView.setGravity(Gravity.CENTER);
            textView.setMinHeight(height * classLength);
            textView.setMaxHeight(height * classLength);
            textView.setTextSize(10);
            index.addView(textView);
        }
    }

    private void addContentView(ViewGroup content, List<ClassInfo> classes, boolean onlyOneWeek, int thisWeek) {
        content.removeAllViews();
        if (classes.size() < 7 * dailyClasses) return;
        for (int i = 0; i < 7; i++) {
            colorIndex = i;
            if (colorIndex > Constants.colorString.length) {
                colorIndex /= 3;
            }
            for (int j = 0; j < dailyClasses; j++) {
                colorIndex++;
                if (colorIndex >= Constants.colorString.length) {
                    colorIndex = 0;
                }
                ClassInfo classInfo = classes.get(j * 7 + i);
                if (classInfo == null) {
                    continue;
                }

                int x = i * width;
                int y = j * height * classLength;
                if (onlyOneWeek) {
                    if (classInfo.hasClass(thisWeek)) {
                        addClassInfoView(content, classInfo, x, y);
                    }
                    while (classInfo.hasSubClass()) {
                        classInfo = classInfo.getSubClassInfo();
                        if (classInfo.hasClass(thisWeek)) {
                            addClassInfoView(content, classInfo, x, y);
                        }
                    }
                } else {
                    addClassInfoView(content, classInfo, x, y);
                }
            }
        }
    }

    private void addClassInfoView(final ViewGroup content, final ClassInfo classInfo, int x, int y) {
        // exclude empty classInfo
        if (classInfo.isEmpty()) return;

        final CardView cardView = (CardView) LayoutInflater.from(mContext).inflate(R.layout.item_class_info, null);
        TextView classInfoView = (TextView) cardView.findViewById(R.id.class_name);
        classInfoView.setText(classInfo.getName() + "@" + classInfo.getPlace());

        int h = classInfo.getLength() * height;

        if (h < 0 || h >= classLength * dailyClasses * height)
            h = height * classLength;

        cardView.setX(x);
        cardView.setY(y);

        cardView.setCardBackgroundColor(Constants.getColor(colorIndex));

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classInfo.getAlertDialog(mContext, mPresenter).show();
            }
        });

        content.addView(cardView);

        ViewGroup.LayoutParams params = cardView.getLayoutParams();
        params.width = width;
        params.height = h;
    }

    @Override
    public void updateClasses(List<ClassInfo> classes) {
        int week = Loader.getCurrentWeek(mContext);
        mTitles[1] = "本周 (第" + week + "周)";

        mTodayClassAdapter.setNewTodayClasses(classes, week);
        mTodayClassAdapter.notifyDataSetChanged();

        View view = mViewMap.get("ts");
        ViewGroup seq = (ViewGroup) view.findViewById(R.id.sem_class_seq);
        ViewGroup con = (ViewGroup) view.findViewById(R.id.sem_class_content);
        addSeqViews(seq);
        addContentView(con, classes, false, -1);
        showSelectedWeek(classes, week);

        if (!showedPrompt) {
            showedPrompt = true;
            if (mTodayClassAdapter.hasClassToday()) {
                int count = mTodayClassAdapter.getItemCount();
                Snackbar.make(mViewPager, "今天有 " + count + " 节课", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(mViewPager, "今天没有课, 好好休息一下吧~", Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    private void showSelectedWeek(List<ClassInfo> classes, int week) {
        View view = mViewMap.get("tw");
        ViewGroup seq = (ViewGroup) view.findViewById(R.id.class_seq);
        ViewGroup con = (ViewGroup) view.findViewById(R.id.class_content);
        addSeqViews(seq);
        addContentView(con, classes, true, week);
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
    }

}
