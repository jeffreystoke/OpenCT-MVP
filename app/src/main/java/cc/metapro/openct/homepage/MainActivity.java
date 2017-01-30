package cc.metapro.openct.homepage;

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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.LibBorrowActivity;
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.customviews.InitDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;

@Keep
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    ClassContract.Presenter mPresenter;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private boolean mExitState;

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

        String initStr = getString(R.string.pref_init);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean init = preferences.getBoolean(initStr, false);
        if (!init) {
            InitDialog.newInstance().show(getSupportFragmentManager(), "init_dialog");
        }
        // add class fragment
        FragmentManager fm = getSupportFragmentManager();
        ClassFragment classFragment = (ClassFragment) fm.findFragmentById(R.id.classes_container);

        if (classFragment == null) {
            classFragment = new ClassFragment();
            ActivityUtils.addFragmentToActivity(fm, classFragment, R.id.classes_container);
        }
        mPresenter = new ClassPresenter(classFragment, this);
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
        if (id == R.id.refresh_classes) {
            Map<String, String> map = Loader.getCmsStuInfo(this);
            if (map.size() < 2) {
                Toast.makeText(this, R.string.enrich_cms_info, Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            } else {
                if (Loader.cmsNeedCAPTCHA(this)) {
                    CaptchaDialog.newInstance(mPresenter).show(getSupportFragmentManager(), "captcha_dialog");
                } else {
                    mPresenter.loadOnline("");
                }
            }
            return true;
        } else if (id == R.id.export_classes) {
            mPresenter.exportClasses();
        } else if (id == R.id.add_class) {
            ClassAddDialog.newInstance("添加课程", null, new ClassAddDialog.ClassAddCallBack() {
                @Override
                public void onAdded(EnrichedClassInfo classInfo) {
                    mPresenter.onClassEdited(classInfo);
                }
            }).show(getSupportFragmentManager(), "class_add_dialog");
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_grades:
                startActivity(new Intent(this, GradeActivity.class));
                break;
            case R.id.nav_search:
                startActivity(new Intent(this, LibSearchActivity.class));
                break;
            case R.id.nav_borrow_info:
                startActivity(new Intent(this, LibBorrowActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
//        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStatic();
    }

    private void initStatic() {
        if (Constants.CLASS_BASE_HEIGHT == 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Constants.CLASS_WIDTH = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
            Constants.CLASS_BASE_HEIGHT = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));
        }

        Constants.CAPTCHA_FILE = getCacheDir().getPath() + "/captcha";
        Constants.USERNAME_KEY = getString(R.string.key_username);
        Constants.PASSWORD_KEY = getString(R.string.key_password);
        Constants.CAPTCHA_KEY = getString(R.string.key_captcha);
        Constants.ACTION_KEY = getString(R.string.key_action);
        Constants.SEARCH_TYPE_KEY = getString(R.string.key_search_type);
        Constants.SEARCH_CONTENT_KEY = getString(R.string.key_search_content);
    }
}
