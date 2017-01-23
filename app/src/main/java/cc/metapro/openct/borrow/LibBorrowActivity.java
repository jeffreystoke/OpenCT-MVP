package cc.metapro.openct.borrow;

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
import android.support.annotation.Keep;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
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
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;

@Keep
public class LibBorrowActivity extends AppCompatActivity {

    @BindView(R.id.lib_borrow_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    private CaptchaDialog mCaptchaDialog;

    private LibBorrowContract.Presenter mPresenter;

    private LibBorrowFragment mFragment;

    @OnClick(R.id.fab)
    public void load() {
        Map<String, String> map = Loader.getLibStuInfo(this);
        if (map.size() < 2) {
            Toast.makeText(this, R.string.enrich_lib_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (Loader.libNeedCAPTCHA()) {
                mCaptchaDialog.show(getSupportFragmentManager(), "captcha_dialog");
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
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // add fragment
        FragmentManager fm = getSupportFragmentManager();
        mFragment = (LibBorrowFragment) fm.findFragmentById(R.id.lib_borrow_container);

        if (mFragment == null) {
            mFragment = new LibBorrowFragment();
            ActivityUtils.addFragmentToActivity(fm, mFragment, R.id.lib_borrow_container);
        }
        mPresenter = new LibBorrowPresenter(mFragment, this);

        mCaptchaDialog = CaptchaDialog.newInstance(mPresenter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all_borrow_info:
                mFragment.onLoadBorrows(mPresenter.getBorrows());
                break;
            case R.id.show_due_borrow_info:
                mFragment.showDue(mPresenter.getBorrows());
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