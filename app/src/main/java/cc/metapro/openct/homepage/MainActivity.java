package cc.metapro.openct.homepage;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.LibBorrowActivity;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.customviews.InitDiaolgHelper;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.gradelist.GradeActivity;
import cc.metapro.openct.preference.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private boolean mExitState;

    private ActivityUtils.CaptchaDialogHelper mCaptchaHelper;

    private AlertDialog mAlertDialog;

    private ClassContract.Presenter mPresenter;

    private ClassFragment mClassFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        mExitState = false;

        ActionBarDrawerToggle toggle = new
                ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean inited = preferences.getBoolean(Constants.PREF_INITED, false);
        if (!inited) {
            new InitDiaolgHelper(this).getInitDialog().show();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.PREF_INITED, true);
            editor.apply();
        } else {
            Loader.loadUniversity(this);
        }

        // add class fragment
        FragmentManager fm = getSupportFragmentManager();
        mClassFragment =
                (ClassFragment) fm.findFragmentById(R.id.classes_container);

        if (mClassFragment == null) {
            mClassFragment = new ClassFragment();
            ActivityUtils.addFragmentToActivity(fm, mClassFragment, R.id.classes_container);
        }
        mPresenter = new ClassPresenter(mClassFragment, this);

        mCaptchaHelper = new ActivityUtils.CaptchaDialogHelper(this, mPresenter, "更新课表");
        mAlertDialog = mCaptchaHelper.getCaptchaDialog();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (mExitState) {
                finish();
            } else {
                Toast.makeText(this, "再按一次返回键退出", Toast.LENGTH_SHORT).show();
                mExitState = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mExitState = false;
                    }
                }, 2000);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.refresh_classes) {
            Map<String, String> map = Loader.getCmsStuInfo(this);
            if (map.size() < 2) {
                Toast.makeText(this, R.string.enrich_cms_info, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } else {
                if (Loader.cmsNeedCAPTCHA()) {
                    mPresenter.loadCaptcha(mCaptchaHelper.getCaptchaView());
                    mAlertDialog.show();
                } else {
                    mPresenter.loadOnline("");
                }
            }
            return true;
        } else if (id == R.id.export_classes) {
            mPresenter.exportCLasses();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.nav_grade_info:
                intent = new Intent(this, GradeActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_lib_search:
                intent = new Intent(this, LibSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_lib_borrow_info:
                intent = new Intent(this, LibBorrowActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_custom:
                intent = new Intent(this, CustomActivity.class);
                startActivity(intent);
//            case R.id.nav_empty_room:
//                intent = new Intent(this, RoomActivity.class);
//                startActivity(intent);
//                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
