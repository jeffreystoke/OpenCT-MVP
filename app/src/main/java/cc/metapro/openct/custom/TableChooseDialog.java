package cc.metapro.openct.custom;

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

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;

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
        final List<Element> rawInfoList = getClasses(targetTable);
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
                public void onFinsih(Map<String, Integer> indexMap) {
                    CmsFactory.ClassTableInfo info = new CmsFactory.ClassTableInfo();
                    info.mNameIndex = indexMap.get(TableSettingDialog.NAME);
                    info.mTimeIndex = indexMap.get(TableSettingDialog.TIME);
                    info.mDuringIndex = indexMap.get(TableSettingDialog.DURING);
                    info.mPlaceIndex = indexMap.get(TableSettingDialog.PLACE);
                    info.mTeacherIndex = indexMap.get(TableSettingDialog.TEACHER);
                    info.mClassTableID = tableId;
                    info.mDailyClasses = rawInfoList.size() / 7;
                    info.mDuringRE = "(?<=(\\{第))\\d+.*?\\d+(?=(周))";
                    info.mTimeRE = "((?<=\\|)(\\d+)?.*?\\d+(?=节))|(\\d+.?\\d+(?=(节\\{)))";

                    List<EnrichedClassInfo> classes = generateClasses(rawInfoList, info, getActivity().getResources().getDisplayMetrics());
                    AdvancedCustomInfo advInfo = new AdvancedCustomInfo();
                    advInfo.setClassTableInfo(info);
                    advInfo.setWebScriptConfiguration(CustomActivity.webScriptConfig);
                    advInfo.mCmsClassURL = CustomActivity.cmsClassURL;

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

    public static List<Element> getClasses(Element table) {
        Pattern pattern = Pattern.compile("(\\d+.*\\d+节$)|(\\d+节$)");
        List<Element> tdWithClassInfo = new ArrayList<>();
        for (Element tr : table.select("tr")) {
            Elements tds = tr.select("td");
            Element td = tds.first();
            boolean found = false;
            while (td != null) {
                if (pattern.matcher(td.text()).find()) {
                    td = td.nextElementSibling();
                    found = true;
                    break;
                }
                td = td.nextElementSibling();
            }
            if (!found) {
                continue;
            }
            int i = 0;
            while (td != null) {
                i++;
                tdWithClassInfo.add(td);
                td = td.nextElementSibling();
            }
            // 补足七天
            for (; i < 7; i++) {
                tdWithClassInfo.add(new Element(Tag.valueOf("td"), table.baseUri()));
            }
        }
        return tdWithClassInfo;
    }

    public static List<EnrichedClassInfo> generateClasses(List<Element> rawInfo, CmsFactory.ClassTableInfo info, DisplayMetrics metrics) {
        List<ClassInfo> classes = new ArrayList<>(rawInfo.size());
        for (Element td : rawInfo) {
            classes.add(new ClassInfo(td.text(), info));
        }
        List<EnrichedClassInfo> enrichedClasses = new ArrayList<>(classes.size());
        int dailyClasses = classes.size() / 7;
        int baseLength = Loader.getClassLength();
        final int width = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
        final int baseHeight = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));
        final int classLength = Loader.getClassLength();

        for (int i = 0; i < 7; i++) {
            int colorIndex = i;
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
                // 计算坐标
                int x = i * width;
                int y = j * baseHeight * classLength;

                if (!classInfo.isEmpty()) {
                    int h = classInfo.getLength() * baseHeight;
                    if (h <= 0 || h > dailyClasses * baseHeight) {
                        h = baseHeight * baseLength;
                    }
                    enrichedClasses.add(new EnrichedClassInfo
                            (classInfo, x, y, Constants.getColor(colorIndex), width, h, i + 1));

                }
            }
        }

        return enrichedClasses;
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
