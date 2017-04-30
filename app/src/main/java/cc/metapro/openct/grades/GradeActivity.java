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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.data.LocalUser;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.grades.cet.CETQueryDialog;
import cc.metapro.openct.grades.cet.CETResultDialog;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class GradeActivity extends BaseActivity implements GradeContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.image)
    ImageView mImage;

    private GradeContract.Presenter mPresenter;
    private GradeAdapter mGradeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mGradeAdapter = new GradeAdapter(this);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mGradeAdapter);
        new GradePresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_grade;
    }

    @OnClick(R.id.fab)
    public void refresh() {
        LocalUser user = LocalHelper.getCmsStuInfo(this);
        if (user.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_cms_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            mPresenter.loadOnlineInfo(getSupportFragmentManager());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onLoadGrades(List<GradeInfo> grades) {
        mGradeAdapter.updateGrades(grades);
        mGradeAdapter.notifyDataSetChanged();
    }

    @Override
    public void showCETDialog() {
        CETQueryDialog.newInstance(mPresenter)
                .show(getSupportFragmentManager(), "cet_query");
    }

    @Override
    public void onLoadCETGrade(Map<String, String> resultMap) {
        CETResultDialog.newInstance(resultMap)
                .show(getSupportFragmentManager(), "cet_result");
    }

    @Override
    public void setPresenter(GradeContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grade, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.clear) {
            onLoadGrades(new ArrayList<GradeInfo>(0));
            mPresenter.clearGrades();
        } else if (id == R.id.query) {
            showCETDialog();
        } else if (id == R.id.custom) {
            CustomActivity.actionStart(this, Constants.TYPE_GRADE);
        }
        return super.onOptionsItemSelected(item);
    }
}
