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
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.LocalUser;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.model.BorrowInfo;
import cc.metapro.openct.pref.SettingsActivity;
import cc.metapro.openct.utils.RecyclerViewHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class BorrowActivity extends BaseActivity implements BorrowContract.View {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.image)
    ImageView mImage;

    private BorrowContract.Presenter mPresenter;
    private BorrowAdapter mBorrowAdapter;

    @OnClick(R.id.fab)
    public void load() {
        LocalUser user = LocalHelper.getLibStuInfo(this);
        if (user.isEmpty()) {
            Toast.makeText(this, R.string.please_fill_lib_info, Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            mPresenter.loadOnlineInfo(getSupportFragmentManager());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        new BorrowPresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_borrow;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(BorrowContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void updateBorrows(List<BorrowInfo> borrows) {
        if (borrows == null) {
            Snackbar.make(mRecyclerView, R.string.no_borrows, BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            mBorrowAdapter.setNewBorrows(borrows);
            mBorrowAdapter.notifyDataSetChanged();
            Snackbar.make(mRecyclerView, getString(R.string.borrow_entries, borrows.size()), BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_all:
                mPresenter.showAll();
                break;
            case R.id.show_due:
                mPresenter.showDue();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.borrow, menu);
        return true;
    }
}