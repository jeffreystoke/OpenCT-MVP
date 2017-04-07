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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.BorrowContract;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.grades.GradeContract;
import cc.metapro.openct.myclass.ClassContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.webutils.TableUtils;

public class TableChooseDialog extends DialogFragment {

    private static String TYPE;
    private static Map<String, Element> tableMap;
    private static List<String> tableIds;
    private static LoginPresenter mPresenter;

    ViewPager mViewPager;

    public static TableChooseDialog newInstance(String type, Document source, @Nullable LoginPresenter presenter) {
        tableMap = TableUtils.getTablesFromTargetPage(source);
        TYPE = type;
        mPresenter = presenter;
        return new TableChooseDialog();
    }

    private CmsFactory.ClassTableInfo generateClassTableInfo(CmsFactory.ClassTableInfo baseInfo, Map<String, Integer> indexMap) {
        if (baseInfo == null) {
            baseInfo = new CmsFactory.ClassTableInfo();
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
        if (!tableIds.isEmpty()) {
            final String tableId = tableIds.get(mViewPager.getCurrentItem());
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
                                CmsFactory.ClassTableInfo info = generateClassTableInfo(Constants.advCustomInfo.mClassTableInfo, indexMap);
                                info.mClassTableID = tableId;
                                Classes classes = UniversityUtils.generateClasses(context, rawInfoList, info);
                                Constants.advCustomInfo.setClassTableInfo(info);

                                manger.updateAdvCustomInfo(Constants.advCustomInfo);
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
                    Constants.advCustomInfo.GRADE_TABLE_ID = tableId;
                    manger.updateAdvCustomInfo(Constants.advCustomInfo);
                    manger.updateGrades(UniversityUtils.generateInfo(targetTable, GradeInfo.class));
                    if (mPresenter != null && mPresenter instanceof GradeContract.Presenter) {
                        ((GradeContract.Presenter) mPresenter).loadLocalGrades();
                    }
                    dismiss();
                    break;
                case Constants.TYPE_SEARCH:
                    break;
                case Constants.TYPE_BORROW:
                    Constants.advCustomInfo.BORROW_TABLE_ID = tableId;
                    manger.updateAdvCustomInfo(Constants.advCustomInfo);
                    manger.updateBorrows(UniversityUtils.generateInfo(targetTable, BorrowInfo.class));
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
        tableIds = new ArrayList<>(tableMap.size());
        for (String s : tableMap.keySet()) {
            tableIds.add(s);
        }

        mViewPager = new ViewPager(getActivity());
        mViewPager.setId(R.id.view_pager);
        mViewPager.setAdapter(new FragmentStatePagerAdapter(getFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return HtmlTableFragment.newInstance(tableMap.get(tableIds.get(position)).html());
            }

            @Override
            public int getCount() {
                return tableMap.size();
            }
        });

        Constants.checkAdvCustomInfo(getActivity());
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.swipe_choose_table)
                .setView(mViewPager)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        select();
                    }
                })
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
        TYPE = null;
        tableMap = null;
        tableIds = null;
    }
}
