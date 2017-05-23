package cc.metapro.openct.customviews;

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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.BorrowContract;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.university.ClassTableInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.model.BorrowInfo;
import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.grades.GradeContract;
import cc.metapro.openct.myclass.ClassContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;
import cc.metapro.openct.utils.base.LoginPresenter;
import cc.metapro.openct.utils.webutils.TableUtils;

public class TableChooseDialog extends BaseDialog {

    private static String TYPE;
    private static Map<String, Element> tableMap;
    private static LoginPresenter mPresenter;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;
    @BindView(R.id.tab_bar)
    TabLayout mTabBar;
    private List<String> mTableIds;

    public static TableChooseDialog newInstance(String type, Document source, @Nullable LoginPresenter presenter) {
        tableMap = TableUtils.getTablesFromTargetPage(source);
        TYPE = type;
        mPresenter = presenter;
        return new TableChooseDialog();
    }

    private ClassTableInfo generateClassTableInfo(ClassTableInfo baseInfo, Map<String, Integer> indexMap) {
        if (baseInfo == null) {
            baseInfo = new ClassTableInfo();
        }

        try {
            baseInfo.mNameIndex = indexMap.get(Constants.NAME);
        } catch (Exception e) {
            baseInfo.mNameIndex = 0;
        }
        try {
            baseInfo.mTimeIndex = indexMap.get(Constants.TIME);
        } catch (Exception e) {
            baseInfo.mTimeIndex = 0;
        }
        try {
            baseInfo.mDuringIndex = indexMap.get(Constants.DURING);
        } catch (Exception e) {
            baseInfo.mDuringIndex = 0;
        }
        try {
            baseInfo.mPlaceIndex = indexMap.get(Constants.PLACE);
        } catch (Exception e) {
            baseInfo.mPlaceIndex = 0;
        }
        try {
            baseInfo.mTeacherIndex = indexMap.get(Constants.TEACHER);
        } catch (Exception e) {
            baseInfo.mTeacherIndex = 0;
        }
        try {
            baseInfo.mTypeIndex = indexMap.get(Constants.TYPE);
        } catch (Exception e) {
            baseInfo.mTypeIndex = 0;
        }

        return baseInfo;
    }

    public void select() {
        if (!mTableIds.isEmpty()) {
            final String tableId = mTableIds.get(mViewPager.getCurrentItem());
            final DBManger manger = DBManger.getInstance(getActivity());
            final Context context = getActivity();
            Element targetTable = tableMap.get(tableId);
            switch (TYPE) {
                case Constants.TYPE_CLASS:
                    final List<Element> rawInfoList = UniversityUtils.getRawClasses(targetTable, getActivity());
                    try {
                        TableSettingDialog.newInstance(rawInfoList, new TableSettingDialog.TableSettingCallBack() {
                            @Override
                            public void onResult(Map<String, Integer> indexMap) {
                                ClassTableInfo info = generateClassTableInfo(Constants.sDetailCustomInfo.mClassTableInfo, indexMap);
                                info.mClassTableID = tableId;
                                Classes classes = UniversityUtils.generateClasses(context, rawInfoList, info);
                                Constants.sDetailCustomInfo.setClassTableInfo(info);

                                manger.updateAdvCustomInfo(Constants.sDetailCustomInfo);
                                manger.updateClasses(classes);
                                if (mPresenter != null && mPresenter instanceof ClassContract.Presenter) {
                                    ((ClassContract.Presenter) mPresenter).loadLocalClasses();
                                }
                                Toast.makeText(context, R.string.custom_finish_tip, Toast.LENGTH_LONG).show();
                            }
                        }).show(getFragmentManager(), "setting_dialog");
                        dismiss();
                    } catch (Exception e) {
                        Toast.makeText(context, R.string.sorry_for_unable_to_get_class_info, Toast.LENGTH_LONG).show();
                    }
                    break;
                case Constants.TYPE_GRADE:
                    Constants.sDetailCustomInfo.setGradeTableId(tableId);
                    manger.updateAdvCustomInfo(Constants.sDetailCustomInfo);
                    manger.updateGrades(UniversityUtils.generateInfoFromTable(targetTable, GradeInfo.class));
                    if (mPresenter != null && mPresenter instanceof GradeContract.Presenter) {
                        ((GradeContract.Presenter) mPresenter).loadLocalGrades();
                    }
                    dismiss();
                    break;
                case Constants.TYPE_SEARCH:
                    break;
                case Constants.TYPE_BORROW:
                    Constants.sDetailCustomInfo.setBorrowTableId(tableId);
                    manger.updateAdvCustomInfo(Constants.sDetailCustomInfo);
                    manger.updateBorrows(UniversityUtils.generateInfoFromTable(targetTable, BorrowInfo.class));
                    if (mPresenter != null && mPresenter instanceof BorrowContract.Presenter) {
                        ((BorrowContract.Presenter) mPresenter).loadLocalBorrows();
                    }
                    dismiss();
                    break;
            }
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_table_choose, null);
        ButterKnife.bind(this, view);
        setView();

        Constants.checkAdvCustomInfo(getActivity());
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.swipe_choose_table)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        select();
                    }
                })
                .create();
    }

    private void setView() {
        mTableIds = new ArrayList<>(tableMap.size());
        for (String s : tableMap.keySet()) {
            mTableIds.add(s);
        }

        final List<View> views = new ArrayList<>(mTableIds.size());
        for (String s : mTableIds) {
            TextView textView = new TextView(getActivity());

            String content = tableMap.get(s).toString();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
            } else {
                textView.setText(Html.fromHtml(content));
            }
            views.add(textView);
        }

        mTabBar.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return mTableIds.size();
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return mTableIds.get(position);
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
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
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
        TYPE = null;
        tableMap = null;
        mTableIds = null;
    }
}
