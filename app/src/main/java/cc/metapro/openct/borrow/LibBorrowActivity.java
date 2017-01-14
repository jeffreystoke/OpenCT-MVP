package cc.metapro.openct.borrow;

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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.preference.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;

public class LibBorrowActivity extends AppCompatActivity {

    @BindView(R.id.lib_borrow_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_refresh)
    FloatingActionButton mFab;

    ActivityUtils.CaptchaDialogHelper mCaptchaHelper;

    private AlertDialog mCaptchaDialog;

    private LibBorrowContract.Presenter mPresenter;

    private LibBorrowFragment mLibBorrowFragment;

    @OnClick(R.id.fab_refresh)
    public void load() {
        Map<String, String> map = Loader.getLibStuInfo(this);
        if (map.size() < 2) {
            Toast.makeText(this, R.string.enrich_lib_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (Loader.libNeedCAPTCHA()) {
                mCaptchaDialog.show();
                mPresenter.loadCaptcha(mCaptchaHelper.getCaptchaView());
            } else {
                mPresenter.loadOnline("");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lib_borrow);

        ButterKnife.bind(this);

        // set toolbar
        mToolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_filter));
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mLibBorrowFragment =
                (LibBorrowFragment) fm.findFragmentById(R.id.lib_borrow_container);

        if (mLibBorrowFragment == null) {
            mLibBorrowFragment = new LibBorrowFragment();
            ActivityUtils.addFragmentToActivity(fm, mLibBorrowFragment, R.id.lib_borrow_container);
        }
        mPresenter = new LibBorrowPresenter(mLibBorrowFragment, this);

        mCaptchaHelper = new ActivityUtils.CaptchaDialogHelper(this, mPresenter, "刷新");
        mCaptchaDialog = mCaptchaHelper.getCaptchaDialog();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all_borrow_info:
                mLibBorrowFragment.onLoadBorrows(mPresenter.getBorrows());
                break;
            case R.id.show_due_borrow_info:
                mLibBorrowFragment.showDue(mPresenter.getBorrows());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.borrow_menu, menu);
        return true;
    }
}