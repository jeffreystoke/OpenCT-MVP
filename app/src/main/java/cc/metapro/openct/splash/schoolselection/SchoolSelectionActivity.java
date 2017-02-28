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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.utils.PrefHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class SchoolSelectionActivity
        extends AppCompatActivity implements SearchView.OnQueryTextListener {

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
        setContentView(R.layout.activity_school_selection);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        result = PrefHelper.getString(this, R.string.pref_school_name, "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityUtils.getProgressDialog(this, R.string.loading_school_list).show();
        Observable<SchoolAdapter> observable = Observable.create(new ObservableOnSubscribe<SchoolAdapter>() {
            @Override
            public void subscribe(ObservableEmitter<SchoolAdapter> e) throws Exception {
                mAdapter = new SchoolAdapter(SchoolSelectionActivity.this);
                e.onNext(mAdapter);
            }
        });

        Observer<SchoolAdapter> observer = new MyObserver<SchoolAdapter>(TAG) {
            @Override
            public void onNext(SchoolAdapter schoolAdapter) {
                ActivityUtils.dismissProgressDialog();
                setViews();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    private void setViews() {
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                result = mAdapter.getItem(position).toString();
                Intent intent = new Intent();
                intent.putExtra(SCHOOL_RESULT, result);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        mSearchView.onActionViewExpanded();
        mSearchView.setSubmitButtonEnabled(true);
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (!TextUtils.isEmpty(query)) {
            mAdapter.setTextFilter(query);
            mAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mAdapter.clearTextFilter();
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.setTextFilter(newText);
            mAdapter.notifyDataSetChanged();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        PrefHelper.putString(this, R.string.pref_school_name, result);
        Loader.needUpdateUniversity();
        super.onDestroy();
    }
}
