package cc.metapro.openct.gradelist;


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
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.preference.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class GradeFragment extends Fragment implements GradeContract.View {

    @BindView(R.id.grade_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab_refresh)
    FloatingActionButton fab;

    private Context mContext;
    private AlertDialog.Builder ab;
    private AlertDialog mCaptchaDialog;
    private GradeAdapter mGradeAdapter;
    private GradeContract.Presenter mPresenter;
    private ActivityUtils.CaptchaDialogHelper mCaptchaDialogHelper;

    @OnClick(R.id.fab_refresh)
    public void refresh() {
        Map<String, String> map = Loader.getCmsStuInfo(mContext);
        if (map.size() == 0) {
            Toast.makeText(mContext, R.string.enrich_cms_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(mContext, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (Loader.cmsNeedCAPTCHA()) {
                mCaptchaDialog.show();
                mPresenter.loadCaptcha(mCaptchaDialogHelper.getCaptchaView());
            } else {
                mPresenter.loadOnline("");
            }
        }
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_grade, container, false);
        ButterKnife.bind(this, view);
        mContext = getContext();

        mCaptchaDialogHelper = new ActivityUtils.CaptchaDialogHelper(getContext(), mPresenter, "刷新");
        mCaptchaDialog = mCaptchaDialogHelper.getCaptchaDialog();

        mGradeAdapter = new GradeAdapter(mContext);

        RecyclerViewHelper.setRecyclerView(mContext, mRecyclerView, mGradeAdapter);
        return view;
    }

    @Override
    public void onResume() {
        mPresenter.start();
        super.onResume();
    }

    @Override
    public void onLoadGrades(List<GradeInfo> infos) {
        mGradeAdapter.updateGradeInfos(infos);
        mGradeAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void showCETDialog() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cet_query, null);
        final EditText num = (EditText) view.findViewById(R.id.cet_cert_num);
        final EditText name = (EditText) view.findViewById(R.id.cet_cert_name);

        ab = new AlertDialog.Builder(mContext);
        ab.setPositiveButton("查询", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String n = num.getText().toString();
                String na = name.getText().toString();
                if (!Strings.isNullOrEmpty(n) && !Strings.isNullOrEmpty(na)) {
                    Map<String, String> queryMap = new HashMap<>(2);
                    queryMap.put(Constants.CET_NUM_KEY, n);
                    queryMap.put(Constants.CET_NAME_KEY, na);
                    mPresenter.loadCETGrade(queryMap);
                    ActivityUtils.getProgressDialog(mContext, R.string.loading_cet_grade).show();
                }
            }
        });
        ab.setTitle("CET 成绩查询");
        ab.setNegativeButton("取消", null);
        ab.setCancelable(false);
        ab.setView(view);
        ab.show();
    }

    @Override
    public void onLoadCETGrade(Map<String, String> resultMap) {
        String name = resultMap.get(Constants.CET_NAME_KEY);
        String school = resultMap.get(Constants.CET_SCHOOL_KEY);
        String type = resultMap.get(Constants.CET_TYPE_KEY);
        String num = resultMap.get(Constants.CET_NUM_KEY);
        String time = resultMap.get(Constants.CET_TIME_KEY);
        String grade = resultMap.get(Constants.CET_GRADE_KEY);

        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_cet_result, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.cet_result_layout);
        if (!Strings.isNullOrEmpty(name)) {
            TextView textView = new TextView(mContext);
            textView.setText("姓名: " + name);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(school)) {
            TextView textView = new TextView(mContext);
            textView.setText("学校: " + school);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(type)) {
            TextView textView = new TextView(mContext);
            textView.setText("CET类型: " + type);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(num)) {
            TextView textView = new TextView(mContext);
            textView.setText("准考证号: " + num);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(time)) {
            TextView textView = new TextView(mContext);
            textView.setText("考试时间: " + time);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        if (!Strings.isNullOrEmpty(grade)) {
            TextView textView = new TextView(mContext);
            textView.setText("成绩: " + grade);
            textView.setTextSize(15);
            textView.setPadding(10, 10, 10, 10);
            layout.addView(textView);
        }

        ab.setPositiveButton("好的", null);
        ab.setNegativeButton("", null);
        ab.setView(view);
        ab.show();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
