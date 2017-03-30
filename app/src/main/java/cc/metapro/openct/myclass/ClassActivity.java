package cc.metapro.openct.myclass;

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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import cc.metapro.openct.allclasses.AllClassesActivity;
import cc.metapro.openct.borrow.BorrowActivity;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

public class ClassActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ClassContract.View {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.nav_view)
    NavigationView mNavigationView;
    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;
    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    ClassContract.Presenter mPresenter;
    private boolean mExitState;
    private ClassPagerAdapter mClassPagerAdapter;

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

        mTabLayout.setupWithViewPager(mViewPager);

        mClassPagerAdapter = new ClassPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mClassPagerAdapter);

        int index = Integer.parseInt(PrefHelper.getString(this, R.string.pref_homepage_selection, "0"));
        mViewPager.setCurrentItem(index);

        new ClassPresenter(this, this);
    }

    @Override
    public void updateClasses(@NonNull Classes classes) {
        mClassPagerAdapter.startUpdate(mViewPager);
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
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
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                mPresenter.loadOnlineInfo(getSupportFragmentManager());
            }
            return true;
        } else if (id == R.id.edit_classes) {
            startActivity(new Intent(this, AllClassesActivity.class));
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
                startActivity(new Intent(this, BorrowActivity.class));
                break;
            case R.id.nav_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initStatic();
        mPresenter.start();
        mClassPagerAdapter.notifyDataSetChanged();
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
                Toast.makeText(this, R.string.one_more_press_to_exit, Toast.LENGTH_SHORT).show();
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

    private void initStatic() {
        if (Constants.CLASS_BASE_HEIGHT == 0) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            Constants.CLASS_WIDTH = (int) Math.round(metrics.widthPixels * (2.0 / 15.0));
            Constants.CLASS_BASE_HEIGHT = (int) Math.round(metrics.heightPixels * (1.0 / 15.0));
        }

        Constants.NAME = getString(R.string.class_name);
        Constants.TIME = getString(R.string.class_time);
        Constants.TYPE = getString(R.string.class_type);
        Constants.DURING = getString(R.string.class_during);
        Constants.PLACE = getString(R.string.class_place);
        Constants.TEACHER = getString(R.string.class_teacher);

        Constants.CAPTCHA_FILE = getCacheDir().getPath() + "/captcha";
        Constants.USERNAME_KEY = getString(R.string.key_username);
        Constants.PASSWORD_KEY = getString(R.string.key_password);
        Constants.CAPTCHA_KEY = getString(R.string.key_captcha);
        Constants.ACTION_KEY = getString(R.string.key_action);
        Constants.SEARCH_TYPE_KEY = getString(R.string.key_search_type);
        Constants.SEARCH_CONTENT_KEY = getString(R.string.key_search_content);
        Constants.DAILY_CLASSES = Integer.parseInt(PrefHelper.getString(this, R.string.pref_daily_class_count, "12"));
    }

}
