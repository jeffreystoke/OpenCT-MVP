package cc.metapro.openct.splash.schoolselection;

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
import android.support.design.widget.Snackbar;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.base.BaseActivity;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SchoolSelectionActivity
        extends BaseActivity implements SearchView.OnQueryTextListener {

    public static final int REQUEST_SCHOOL_NAME = 1;
    public static final String SCHOOL_RESULT = "school_name";
    private static final String TAG = SchoolSelectionActivity.class.getName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.school_name)
    SearchView mSearchView;
    @BindView(R.id.school_list_view)
    StickyListHeadersListView mListView;

    private String result;
    private SchoolAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        result = PrefHelper.getString(this, R.string.pref_school_name, "");
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_school_selection;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schools, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.update:
                updateSchools(true);
                break;
            case R.id.report:
                Toast.makeText(this, R.string.tmp_add_school_promt, Toast.LENGTH_LONG).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSchools(false);
    }

    private void updateSchools(final boolean online) {
        ActivityUtils.showProgressDialog(this, R.string.loading_school_list);

        Observable<SchoolAdapter> observable = Observable.create(new ObservableOnSubscribe<SchoolAdapter>() {
            @Override
            public void subscribe(ObservableEmitter<SchoolAdapter> e) throws Exception {
                if (online) {
                    // delete current schools
                    ActivityUtils.dismissProgressDialog();
                    DBManger.updateSchools(SchoolSelectionActivity.this, null);
                }

                mAdapter = new SchoolAdapter(SchoolSelectionActivity.this);
                e.onNext(mAdapter);
            }
        });

        Observer<SchoolAdapter> observer = new MyObserver<SchoolAdapter>(TAG) {
            @Override
            public void onNext(SchoolAdapter schoolAdapter) {
                if (!online) {
                    ActivityUtils.dismissProgressDialog();
                }
                setViews(schoolAdapter);
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void setViews(final SchoolAdapter mAdapter) {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                result = mAdapter.getItem(position).toString();
                Snackbar.make(mListView, getString(R.string.selected_school, result), BaseTransientBottomBar.LENGTH_INDEFINITE)
                        .setAction(android.R.string.ok, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent();
                                intent.putExtra(SCHOOL_RESULT, result);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        }).show();
            }
        });
        mListView.requestFocus();

        mSearchView.onActionViewExpanded();
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mAdapter.setTextFilter(query)
                .subscribeWith(new MyObserver("") {
                    @Override
                    public void onNext(Object o) {
                        mAdapter.notifyDataSetChanged();
                        mListView.smoothScrollToPosition(0);
                    }
                });
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.setTextFilter(newText)
                .subscribeWith(new MyObserver("") {
                    @Override
                    public void onNext(Object o) {
                        mAdapter.notifyDataSetChanged();
                        mListView.smoothScrollToPosition(0);
                    }
                });
        return true;
    }

    @Override
    protected void onDestroy() {
        PrefHelper.putString(this, R.string.pref_school_name, result);
        LocalHelper.needUpdateUniversity();
        super.onDestroy();
    }
}
