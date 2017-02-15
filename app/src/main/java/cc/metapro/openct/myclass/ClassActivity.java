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
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.borrow.BorrowActivity;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.Constants;

@Keep
public class ClassActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, ClassContract.View {

    private static final int REQUEST_WRITE_STORAGE = 112;

    private static boolean showedPrompt;
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
    private DailyClassAdapter mDailyClassAdapter;
    private ClassPagerAdapter mClassPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initStatic();
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mExitState = false;

        ActionBarDrawerToggle toggle = new
                ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();
        mNavigationView.setNavigationItemSelectedListener(this);
        new ClassPresenter(this, this);
        initViewPager();
    }

    private void initViewPager() {
        mTabLayout.setupWithViewPager(mViewPager);

        mClassPagerAdapter = new ClassPagerAdapter(mViewPager);
        mDailyClassAdapter = mClassPagerAdapter.getDailyClassAdapter();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String homeIndex = preferences.getString(getString(R.string.pref_homepage_selection), "0");
        int index = Integer.parseInt(homeIndex);
        mViewPager.setCurrentItem(index);
    }

    private void addSeqViews(ViewGroup index) {
        if (index != null) {
            index.removeAllViews();
            for (int i = 1; i <= Constants.DAILY_CLASSES; i++) {
                TextView textView = new TextView(this);
                textView.setText("第\n" + i + "\n节");
                textView.setGravity(Gravity.CENTER);
                textView.setMinHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
                textView.setMaxHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
                textView.setTextSize(10);
                index.addView(textView);
            }
        }
    }

    private void addContentView(ViewGroup content, List<EnrichedClassInfo> classes, int thisWeek) {
        if (content == null) return;
        content.removeAllViews();
        for (EnrichedClassInfo info : classes) {
            info.addViewTo(content, this, thisWeek);
        }
    }

    @Override
    public void updateClasses(@NonNull List<EnrichedClassInfo> classes) {
        int week = Loader.getCurrentWeek(this);
        // 更新学期课表视图
        View view = mClassPagerAdapter.getSemClassView();
        ViewGroup seq = (ViewGroup) view.findViewById(R.id.seq);
        ViewGroup con = (ViewGroup) view.findViewById(R.id.content);
        if (!classes.isEmpty()) {
            addSeqViews(seq);
            addContentView(con, classes, -1);
        } else {
            seq.removeAllViews();
            con.removeAllViews();
        }

        // 更新周课表视图
        mClassPagerAdapter.setWeekTitle(week);
        mClassPagerAdapter.notifyDataSetChanged();
        view = mClassPagerAdapter.getWeekClassView();
        seq = (ViewGroup) view.findViewById(R.id.seq);
        con = (ViewGroup) view.findViewById(R.id.content);
        if (!classes.isEmpty()) {
            addSeqViews(seq);
            addContentView(con, classes, week);
        } else {
            seq.removeAllViews();
            con.removeAllViews();
        }

        // 更新当日课表视图
        mDailyClassAdapter.updateTodayClasses(classes, week);
        mDailyClassAdapter.notifyDataSetChanged();
        if (mDailyClassAdapter.hasClassToday()) {
            mClassPagerAdapter.showRecyclerView();
            if (!showedPrompt) {
                showedPrompt = true;
                Snackbar.make(mViewPager, "今天有 " + mDailyClassAdapter.getItemCount() + " 节课", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mClassPagerAdapter.dismissRecyclerView(preferences.getString(getString(R.string.pref_empty_class_motto), getString(R.string.default_motto)));
            if (!showedPrompt) {
                showedPrompt = true;
                Snackbar.make(mViewPager, R.string.empty_class_tip, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        mPresenter = presenter;
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
                mPresenter.loadOnlineInfo(getSupportFragmentManager());
            }
            return true;
        } else if (id == R.id.export_classes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                }
            } else {
                mPresenter.exportClasses();
            }
        } else if (id == R.id.add_class) {
            ClassDetailActivity.actionStart(this, new EnrichedClassInfo());
        } else if (id == R.id.clear_classes) {
            mPresenter.clearClasses();
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
//        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPresenter.exportClasses();
                } else {
                    Toast.makeText(this, R.string.no_write_permission, Toast.LENGTH_LONG).show();
                }
        }
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
