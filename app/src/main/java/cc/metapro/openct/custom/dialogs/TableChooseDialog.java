package cc.metapro.openct.custom.dialogs;

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

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;

@Keep
public class TableChooseDialog extends DialogFragment {

    private static Map<String, Element> tables;
    private static List<String> titles;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.view_pager_title)
    PagerTabStrip mTabStrip;

    public static TableChooseDialog newInstance(Map<String, Element> source) {
        tables = source;
        return new TableChooseDialog();
    }

    @OnClick(R.id.select)
    public void select() {
        final String tableId = titles.get(mViewPager.getCurrentItem());
        Element targetTable = tables.get(tableId);
        final List<Element> rawInfoList = UniversityUtils.getRawClasses(targetTable);
        Element sample = null;
        for (Element td : rawInfoList) {
            if (td.text().length() > 10) {
                sample = td;
                break;
            }
        }
        // 获取到了样本
        if (sample != null) {
            TableSettingDialog.newInstance(sample.text(), new TableSettingDialog.TableSettingCallBack() {
                @Override
                public void onFinish(Map<String, Integer> indexMap) {
                    CmsFactory.ClassTableInfo info = new CmsFactory.ClassTableInfo();
                    info.mNameIndex = indexMap.get(TableSettingDialog.NAME);
                    info.mTimeIndex = indexMap.get(TableSettingDialog.TIME);
                    info.mDuringIndex = indexMap.get(TableSettingDialog.DURING);
                    info.mPlaceIndex = indexMap.get(TableSettingDialog.PLACE);
                    info.mTeacherIndex = indexMap.get(TableSettingDialog.TEACHER);
                    info.mTypeIndex = indexMap.get(TableSettingDialog.TYPE);

                    info.mClassTableID = tableId;
                    info.mDailyClasses = rawInfoList.size() / 7;
                    info.mDuringRE = "(?<=(\\{第))\\d+.*?\\d+(?=(周))";
                    info.mTimeRE = "((?<=\\|)(\\d+)?.*?\\d+(?=节))|(\\d+.?\\d+(?=(节\\{)))";

                    List<EnrichedClassInfo> classes = UniversityUtils.generateClasses(rawInfoList, info);
                    AdvancedCustomInfo advInfo = new AdvancedCustomInfo();
                    advInfo.setClassTableInfo(info);
                    advInfo.setWebScriptConfiguration(CustomActivity.webScriptConfig);
                    advInfo.setCmsClassURL(CustomActivity.cmsClassURL);

                    DBManger manger = DBManger.getInstance(getActivity());
                    manger.updateAdvancedCustomClassInfo(advInfo);
                    manger.updateClasses(classes);
                    Toast.makeText(getActivity(), "获取课表成功, 请回到主界面查看课表", Toast.LENGTH_LONG).show();
                    dismiss();
                }
            }).show(getFragmentManager(), "setting_dialog");
        }
        // 未获取到样本
        else {
            Toast.makeText(getActivity(), "很遗憾, 没能解析到课程信息", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_table_choose, container);
        ButterKnife.bind(this, view);
        initView(inflater);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    private void initView(LayoutInflater inflater) {
        mTabStrip.setTextColor(Color.WHITE);
        mTabStrip.setTabIndicatorColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
        final List<View> views = new ArrayList<>(tables.size());
        titles = new ArrayList<>(tables.size());
        for (String s : tables.keySet()) {
            View contentView = inflater.inflate(R.layout.item_class_table, null);
            TextView textView = (TextView) contentView.findViewById(R.id.table_content);
            textView.setText(tables.get(s).text());
            views.add(contentView);
            titles.add(s);
        }
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return titles.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(views.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(views.get(position));
                return views.get(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return titles.get(position);
            }
        });
    }
}
