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

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
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
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.BorrowContract;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.grades.GradeContract;
import cc.metapro.openct.myclass.ClassContract;

@Keep
public class TableChooseDialog extends DialogFragment {

    public static final String CLASS_TABLE_DIALOG = "class";
    public static final String GRADE_TABLE_DIALOG = "grade";
    public static final String BORROW_TABLE_DIALOG = "borrow";

    private static String TYPE;

    private static Map<String, Element> tableMap;
    private static List<String> tableIds;

    private static LoginPresenter mPresenter;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    public static TableChooseDialog newInstance(String type, Map<String, Element> source, @Nullable LoginPresenter presenter) {
        tableMap = source;
        TYPE = type;
        mPresenter = presenter;
        return new TableChooseDialog();
    }

    @OnClick(R.id.select)
    public void select() {
        final String tableId = tableIds.get(mViewPager.getCurrentItem());
        final DBManger manger = DBManger.getInstance(getActivity());
        AdvancedCustomInfo infoTmp = manger.getAdvancedCustomInfo(getActivity());
        if (infoTmp == null) {
            infoTmp = new AdvancedCustomInfo(getActivity());
        }
        final AdvancedCustomInfo advInfo = infoTmp;
        Element targetTable = tableMap.get(tableId);
        if (CLASS_TABLE_DIALOG.equals(TYPE)) {
            final List<Element> rawInfoList = UniversityUtils.getRawClasses(targetTable, getActivity());
            try {
                TableSettingDialog.newInstance(rawInfoList, new TableSettingDialog.TableSettingCallBack() {
                    @Override
                    public void onFinish(Map<String, Integer> indexMap) {
                        CmsFactory.ClassTableInfo info = getClassTableInfo(advInfo.mClassTableInfo, indexMap);
                        info.mClassTableID = tableId;
                        List<EnrichedClassInfo> classes = UniversityUtils.generateClasses(getActivity(), rawInfoList, info);
                        advInfo.setClassTableInfo(info);
                        advInfo.setWebScriptConfiguration(CustomActivity.webScriptConfig);
                        advInfo.setCmsURL(CustomActivity.cmsClassURL);

                        manger.updateAdvancedCustomClassInfo(advInfo);
                        manger.updateClasses(classes);
                        if (mPresenter != null && mPresenter instanceof ClassContract.Presenter) {
                            ((ClassContract.Presenter) mPresenter).loadLocalClasses();
                        }
                        Toast.makeText(getContext(), R.string.custom_finish_tip, Toast.LENGTH_LONG).show();
                        dismiss();
                    }
                }).show(getFragmentManager(), "setting_dialog");
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.sorry_for_unable_to_get_class_info, Toast.LENGTH_LONG).show();
            }
        } else if (GRADE_TABLE_DIALOG.equals(TYPE)) {
            advInfo.GRADE_TABLE_ID = tableId;
            manger.updateAdvancedCustomClassInfo(advInfo);
            manger.updateGrades(UniversityUtils.generateInfo(tableMap.get(tableId), GradeInfo.class));
            if (mPresenter != null && mPresenter instanceof GradeContract.Presenter) {
                ((GradeContract.Presenter) mPresenter).loadLocalGrades();
            }
            dismiss();
        } else if (BORROW_TABLE_DIALOG.equals(TYPE)) {
            advInfo.BORROW_TABLE_ID = tableId;
            manger.updateAdvancedCustomClassInfo(advInfo);
            manger.updateBorrows(UniversityUtils.generateInfo(tableMap.get(tableId), BorrowInfo.class));
            if (mPresenter != null && mPresenter instanceof BorrowContract.Presenter) {
                ((BorrowContract.Presenter) mPresenter).loadLocalBorrows();
            }
            dismiss();
        }
    }

    private CmsFactory.ClassTableInfo getClassTableInfo(CmsFactory.ClassTableInfo info, Map<String, Integer> indexMap) {
        if (info == null) {
            info = new CmsFactory.ClassTableInfo();
        }
        try {
            info.mNameIndex = indexMap.get(TableSettingDialog.NAME);
        } catch (Exception e) {
            info.mNameIndex = 0;
        }
        try {
            info.mTimeIndex = indexMap.get(TableSettingDialog.TIME);
        } catch (Exception e) {
            info.mTimeIndex = 0;
        }
        try {
            info.mDuringIndex = indexMap.get(TableSettingDialog.DURING);
        } catch (Exception e) {
            info.mDuringIndex = 0;
        }
        try {
            info.mPlaceIndex = indexMap.get(TableSettingDialog.PLACE);
        } catch (Exception e) {
            info.mPlaceIndex = 0;
        }
        try {
            info.mTeacherIndex = indexMap.get(TableSettingDialog.TEACHER);
        } catch (Exception e) {
            info.mTeacherIndex = 0;
        }
        try {
            info.mTypeIndex = indexMap.get(TableSettingDialog.TYPE);
        } catch (Exception e) {
            info.mTypeIndex = 0;
        }

        return info;
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
        final List<View> views = new ArrayList<>(tableMap.size());
        tableIds = new ArrayList<>(tableMap.size());
        for (String s : tableMap.keySet()) {
            View contentView = inflater.inflate(R.layout.item_class_table, mViewPager, false);
            TextView webView = (TextView) contentView.findViewById(R.id.table_content);
            webView.setText(tableMap.get(s).text() + "\n\n");
            views.add(contentView);
            tableIds.add(s);
        }
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return tableIds.size();
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
                return "";
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
