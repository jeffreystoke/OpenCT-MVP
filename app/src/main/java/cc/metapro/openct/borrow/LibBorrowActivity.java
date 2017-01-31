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
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.RecyclerViewHelper;

@Keep
public class LibBorrowActivity extends AppCompatActivity implements LibBorrowContract.View {

    private final String TAG = LibBorrowActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private LibBorrowContract.Presenter mPresenter;
    private BorrowAdapter mBorrowAdapter;

    @OnClick(R.id.fab)
    public void load() {
        Map<String, String> map = Loader.getLibStuInfo(this);
        if (map.size() < 2) {
            Toast.makeText(this, R.string.enrich_lib_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            if (Loader.libNeedCAPTCHA(this)) {
                ActivityUtils.showCaptchaDialog(getSupportFragmentManager(), mPresenter);
            } else {
                mPresenter.loadTargetPage("");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

        ButterKnife.bind(this);

        // set toolbar
        mToolbar.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_filter));
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBorrowAdapter = new BorrowAdapter(this);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mBorrowAdapter);
        // add fragment
        new LibBorrowPresenter(this, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(LibBorrowContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showDue(@NonNull List<BorrowInfo> borrows) {
        try {
            List<BorrowInfo> dueInfo = new ArrayList<>(borrows.size());
            for (BorrowInfo b : borrows) {
                if (b.isExceeded()) {
                    dueInfo.add(b);
                }
            }
            mBorrowAdapter.setNewBorrows(dueInfo);
            mBorrowAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void showAll(List<BorrowInfo> borrows) {
        try {
            mBorrowAdapter.setNewBorrows(borrows);
            mBorrowAdapter.notifyDataSetChanged();
            ActivityUtils.dismissProgressDialog();
            Snackbar.make(mRecyclerView, "共有 " + borrows.size() + " 条借阅信息", BaseTransientBottomBar.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all:
                showAll(mPresenter.getBorrows());
                break;
            case R.id.show_due:
                showDue(mPresenter.getBorrows());
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.borrow_menu, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}