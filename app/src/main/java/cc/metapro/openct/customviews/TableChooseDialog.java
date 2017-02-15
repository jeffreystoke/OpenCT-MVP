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
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.BorrowContract;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.grades.GradeContract;
import cc.metapro.openct.myclass.ClassContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.webutils.TableUtils;

@Keep
public class TableChooseDialog extends DialogFragment {

    private static String TYPE;
    private static Map<String, Element> tableMap;
    private static List<String> tableIds;
    private static LoginPresenter mPresenter;

    @BindView(R.id.view_pager)
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
            baseInfo.mNameIndex = indexMap.get(TableSettingDialog.NAME);
        } catch (Exception e) {
            baseInfo.mNameIndex = 0;
        }
        try {
            baseInfo.mTimeIndex = indexMap.get(TableSettingDialog.TIME);
        } catch (Exception e) {
            baseInfo.mTimeIndex = 0;
        }
        try {
            baseInfo.mDuringIndex = indexMap.get(TableSettingDialog.DURING);
        } catch (Exception e) {
            baseInfo.mDuringIndex = 0;
        }
        try {
            baseInfo.mPlaceIndex = indexMap.get(TableSettingDialog.PLACE);
        } catch (Exception e) {
            baseInfo.mPlaceIndex = 0;
        }
        try {
            baseInfo.mTeacherIndex = indexMap.get(TableSettingDialog.TEACHER);
        } catch (Exception e) {
            baseInfo.mTeacherIndex = 0;
        }
        try {
            baseInfo.mTypeIndex = indexMap.get(TableSettingDialog.TYPE);
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
                                List<EnrichedClassInfo> classes = UniversityUtils.generateClasses(context, rawInfoList, info);
                                Constants.advCustomInfo.setClassTableInfo(info);

                                manger.updateAdvancedCustomClassInfo(Constants.advCustomInfo);
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
                    manger.updateAdvancedCustomClassInfo(Constants.advCustomInfo);
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
                    manger.updateAdvancedCustomClassInfo(Constants.advCustomInfo);
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
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_table_choose, null);
        ButterKnife.bind(this, view);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.swipe_choose_table)
                .setPositiveButton(android.R.string.ok, null)
                .setView(view)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        select();
                    }
                });
            }
        });

        Constants.checkAdvCustomInfo(getActivity());
        initView();
        return dialog;
    }

    private void initView() {
        LayoutInflater inflater = getActivity().getLayoutInflater();
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

}
