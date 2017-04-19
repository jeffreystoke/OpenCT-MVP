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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.allclasses.AllClassesActivity;
import cc.metapro.openct.borrow.BorrowActivity;
import cc.metapro.openct.customviews.WeekSelectionDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class ClassActivity extends BaseActivity
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
    @BindView(R.id.content_main)
    CoordinatorLayout mBackground;

    ClassContract.Presenter mPresenter;
    private boolean mExitState;
    private ClassPagerAdapter mClassPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mExitState = false;

        ActionBarDrawerToggle toggle = new
                ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);

        initViewPager();
        new ClassPresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void initViewPager() {
        mTabLayout.setupWithViewPager(mViewPager);

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(final TabLayout.Tab tab) {
                if (tab == mTabLayout.getTabAt(1)) {
                    WeekSelectionDialog.newInstance(new WeekSelectionDialog.SelectionCallback() {
                        @Override
                        public void onSelection(int index) {
                            showClasses(Constants.sClasses, index);
                            TabLayout.Tab tab1 = mTabLayout.getTabAt(1);
                            if (tab1 != null) {
                                tab1.setText(getString(R.string.text_current_week, index));
                            }
                        }
                    }).show(getSupportFragmentManager(), "week_selection");
                }
            }
        });
        mClassPagerAdapter = new ClassPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setAdapter(mClassPagerAdapter);

        int index = Integer.parseInt(PrefHelper.getString(this, R.string.pref_homepage_selection, "0"));
        if (index > 1) {
            index = 0;
            PrefHelper.putString(this, R.string.pref_homepage_selection, "0");
        }
        mViewPager.setCurrentItem(index);
    }

    @Override
    public void showClasses(@NonNull Classes classes, int week) {
        mClassPagerAdapter.updateClasses(classes, week);
        mClassPagerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.refresh_classes) {
            Map<String, String> map = Loader.getCmsStuInfo(this);
            if (map.size() < 2) {
                Toast.makeText(this, R.string.please_fill_cms_info, Toast.LENGTH_LONG).show();
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
                Constants.sClasses = null;
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
}
