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

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.jrummyapps.android.colorpicker.ColorShape;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.allclasses.AllClassesActivity;
import cc.metapro.openct.borrow.BorrowActivity;
import cc.metapro.openct.customviews.WeekSelectionDialog;
import cc.metapro.openct.data.LocalUser;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.ReferenceUtils;
import cc.metapro.openct.utils.base.BaseActivity;

public class ClassActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, ClassContract.View {

    private static final int FILE_SELECT_CODE = 101;
    private static final int REQUEST_WRITE_STORAGE = 112;

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
    @BindView(R.id.main_background)
    ImageView mBackground;

    ClassContract.Presenter mPresenter;
    private boolean mExitState;
    private FragmentPagerAdapter mPagerAdapter;
    private DailyFragment mDailyFragment;
    private TableFragment mTableFragment;

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
        mPresenter = new ClassPresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_main;
    }

    private void initViewPager() {
        mDailyFragment = DailyFragment.newInstance();
        mTableFragment = TableFragment.newInstance();

        mPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return mDailyFragment;
                    case 1:
                        return mTableFragment;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.text_daily_classes);
                    case 1:
                        return getString(R.string.text_current_week, LocalHelper.getCurrentWeek(ClassActivity.this));
                }
                return super.getPageTitle(position);
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

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
        mTabLayout.setupWithViewPager(mViewPager);

        int index = Integer.parseInt(PrefHelper.getString(this, R.string.pref_homepage_selection, "0"));
        if (index > 1) {
            index = 0;
            PrefHelper.putString(this, R.string.pref_homepage_selection, "0");
        }
        mViewPager.setCurrentItem(index);
    }

    @Override
    public void showClasses(@NonNull Classes classes, int week) {
        mTableFragment.showClasses(classes, week);
        mDailyFragment.showClasses(classes, week);
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
            LocalUser user = LocalHelper.getCmsStuInfo(this);
            if (user.isEmpty()) {
                Toast.makeText(this, R.string.please_fill_cms_info, Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, SettingsActivity.class));
            } else {
                mPresenter.loadOnlineInfo(getSupportFragmentManager());
            }
            return true;
        } else if (id == R.id.edit_classes) {
            startActivity(new Intent(this, AllClassesActivity.class));
        } else if (id == R.id.set_background) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                } else {
                    showFilerChooser();
                }
            } else {
                showFilerChooser();
            }
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
            case R.id.nav_theme:
                showThemePicker();
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showThemePicker() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        ColorPickerDialog dialog = ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.select_theme)
                .setColorShape(ColorShape.CIRCLE)
                .setAllowCustom(false)
                .setAllowPresets(false)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setShowAlphaSlider(false)
                .setShowColorShades(false)
                .setPresets(getResources().getIntArray(R.array.theme_color))
                .setColor(ReferenceUtils.getThemeColor(this, R.attr.colorPrimary))
                .create();

        final int oldTheme = PrefHelper.getInt(this, R.string.pref_theme_activity, R.style.AppTheme);
        dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int i, @ColorInt int i1) {
                PrefHelper.putInt(ClassActivity.this, R.string.pref_theme_activity, getThemeByColor(i1));
            }

            @Override
            public void onDialogDismissed(int i) {
                int newTheme = PrefHelper.getInt(ClassActivity.this, R.string.pref_theme_activity, R.style.AppTheme);
                if (oldTheme != newTheme) {
                    finish();
                    startActivity(getIntent());
                }
            }
        });
        dialog.show(getFragmentManager(), "color_picker");
    }

    @Override
    protected void onResume() {
        super.onResume();
        String bgUri = PrefHelper.getString(this, R.string.pref_background, "");
        if (!TextUtils.isEmpty(bgUri)) {
            Glide.with(this).load(bgUri).centerCrop().into(mBackground);
        }
        mPresenter.start();
        mPagerAdapter.notifyDataSetChanged();
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

    private void showFilerChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_schedule_file)), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.fail_file_chooser, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri uri = data.getData();
            PrefHelper.putString(this, R.string.pref_background, uri.toString());
            Glide.with(this).load(uri).centerCrop().into(mBackground);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFilerChooser();
            } else {
                Toast.makeText(this, R.string.no_write_permission, Toast.LENGTH_LONG).show();
            }
        }
    }
}
