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

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

@Keep
public class GradeActivity extends AppCompatActivity {

    @BindView(R.id.grade_toolbar)
    Toolbar mToolbar;

    private GradeContract.Presenter mPresenter;
    private GradeFragment mGradeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grade);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        FragmentManager fm = getSupportFragmentManager();
        mGradeFragment =
                (GradeFragment) fm.findFragmentById(R.id.grade_info_container);

        if (mGradeFragment == null) {
            mGradeFragment = new GradeFragment();
            ActivityUtils.addFragmentToActivity(fm, mGradeFragment, R.id.grade_info_container);
        }
        mPresenter = new GradePresenter(mGradeFragment, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.grade_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.grade_clear) {
            mGradeFragment.onLoadGrades(null);
            mPresenter.clearGrades();
        } else if (id == R.id.cet_query) {
            mGradeFragment.showCETDialog();
        }
        return super.onOptionsItemSelected(item);
    }
}
