package cc.metapro.openct.grades;


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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.RecyclerViewHelper;

@Keep
public class GradeFragment extends Fragment implements GradeContract.View {

    @BindView(R.id.grade_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    private Context mContext;
    private GradeAdapter mGradeAdapter;
    private GradeContract.Presenter mPresenter;
    private CaptchaDialog mCaptchaDialog;

    @OnClick(R.id.fab)
    public void refresh() {
        Map<String, String> map = Loader.getCmsStuInfo(mContext);
        if (map.size() == 0) {
            Toast.makeText(mContext, R.string.enrich_cms_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(mContext, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (Loader.cmsNeedCAPTCHA()) {
                mCaptchaDialog.show(getFragmentManager(), "captcha_dialog");
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

        mCaptchaDialog = CaptchaDialog.newInstance(mPresenter);
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
    public void onLoadGrades(List<GradeInfo> grades) {
        mGradeAdapter.updateGradeInfos(grades);
        mGradeAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void showCETDialog() {
        CETQueryDialog
                .newInstance(mPresenter)
                .show(getFragmentManager(), "cet_query");
    }

    @Override
    public void onLoadCETGrade(Map<String, String> resultMap) {
        ActivityUtils.dismissProgressDialog();
        CETResultDialog
                .newInstance(resultMap)
                .show(getFragmentManager(), "cet_result");
    }

    @Override
    public void showFormDialog(Form form) {
        FormDialog.newInstance(form, mPresenter).show(getFragmentManager(), "form_dialog");
    }

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
