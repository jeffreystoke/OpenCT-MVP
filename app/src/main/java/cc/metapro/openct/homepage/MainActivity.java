package cc.metapro.openct.homepage;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import cc.metapro.openct.customviews.CaptchaDialog;
import cc.metapro.openct.customviews.InitDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.grades.GradeActivity;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.search.LibSearchActivity;
import cc.metapro.openct.utils.ActivityUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @BindView(R.id.nav_view)
    NavigationView mNavigationView;

    private boolean mExitState;

    private CaptchaDialog mCaptchaDialog;

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

        String initStr = getString(R.string.pref_init);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean init = preferences.getBoolean(initStr, false);
        if (!init) {
            InitDialog fragment = InitDialog.newInstance();
            fragment.setCancelable(false);
            fragment.show(getSupportFragmentManager(), "init_dialog_fragment");
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

        mCaptchaDialog = CaptchaDialog.newInstance(mPresenter);
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
                    mCaptchaDialog.show(getSupportFragmentManager(), "captcha_dialog");
                } else {
                    mPresenter.loadOnline("");
                }
            }
            return true;
        } else if (id == R.id.custom_cms_class) {
            Intent intent = new Intent(this, CustomActivity.class);
            startActivity(intent);
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
