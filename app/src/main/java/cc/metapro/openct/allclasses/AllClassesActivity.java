package cc.metapro.openct.allclasses;

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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.custom.CustomActivity;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.RecyclerViewHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class AllClassesActivity extends BaseActivity implements AllClassesContract.View {

    private static final int REQUEST_WRITE_STORAGE = 112;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;
    private AllClassesAdapter mAdapter;
    private AllClassesContract.Presenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        new AllClassesPresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_all_classes;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(AllClassesContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.classes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.export_classes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                int hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_STORAGE);
                } else {
                    mPresenter.exportClasses();
                }
            } else {
                mPresenter.exportClasses();
            }
        } else if (id == R.id.clear_classes) {
            mPresenter.clearClasses();
        } else if (id == R.id.custom) {
            CustomActivity.actionStart(this, Constants.TYPE_CLASS);
        } else if (id == R.id.import_from_excel) {
            mPresenter.loadFromExcel(getSupportFragmentManager());
        } else if (id == R.id.add_class) {
            ClassDetailActivity.actionStart(this, getString(R.string.new_class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void updateClasses() {
        mAdapter = new AllClassesAdapter(this);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mAdapter);
        mRecyclerView.setItemViewSwipeEnabled(true);

        mRecyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(final int position) {
                final EnrichedClassInfo toRemove = Constants.sClasses.get(position);
                Constants.sClasses.remove(position);
                mAdapter.notifyDataSetChanged();
                final Snackbar snackbar = Snackbar.make(mRecyclerView, toRemove.getName() + " " + getString(R.string.deleted), BaseTransientBottomBar.LENGTH_INDEFINITE);
                snackbar.setAction(android.R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Constants.sClasses.add(toRemove);
                        mAdapter.notifyDataSetChanged();
                        snackbar.dismiss();
                        Snackbar.make(mRecyclerView, toRemove.getName() + " " + getString(R.string.restored), BaseTransientBottomBar.LENGTH_LONG).show();
                        mRecyclerView.smoothScrollToPosition(0);
                    }
                });
                snackbar.show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        mPresenter.storeClasses(Constants.sClasses);
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPresenter.exportClasses();
            } else {
                Toast.makeText(this, R.string.no_write_permission, Toast.LENGTH_LONG).show();
            }
        }
    }
}
