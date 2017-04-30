package cc.metapro.openct.search;

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

import android.os.Bundle;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.EndlessRecyclerOnScrollListener;
import cc.metapro.openct.data.university.model.BookInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;
import cc.metapro.openct.utils.base.BaseActivity;
import io.reactivex.disposables.Disposable;

public class LibSearchActivity extends BaseActivity implements LibSearchContract.View {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.fab)
    FloatingActionButton mFabSearch;
    @BindView(R.id.lib_search_content_edittext)
    EditText mEditText;
    @BindView(R.id.type_spinner)
    AppCompatSpinner mSpinner;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.fab_up)
    FloatingActionButton mFabUp;
    @BindView(R.id.image)
    ImageView mImage;

    private LibSearchContract.Presenter mPresenter;
    private BooksAdapter mAdapter;
    private Disposable mTask;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        // set mToolbar
        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mAdapter = new BooksAdapter(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.item_search_type, getResources().getStringArray(R.array.lib_search_type));
        mSpinner.setAdapter(adapter);
        mLinearLayoutManager = RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mAdapter);
        mEditText.requestFocus();

        // TODO: 17/4/12 load image
        new LibSearchPresenter(this, this);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_search;
    }

    @OnClick(R.id.fab_up)
    public void upToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    @OnClick(R.id.fab)
    public void fabSearch() {
        mTask = mPresenter.search(mSpinner.getSelectedItem().toString(), mEditText.getText().toString());
    }

    @OnEditorAction(R.id.lib_search_content_edittext)
    public boolean editorSearch(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_SEARCH || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            mTask = mPresenter.search(mSpinner.getSelectedItem().toString(), mEditText.getText().toString());
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (mTask != null) {
            mTask.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void showOnSearching() {
        mRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mRecyclerView != null) {
                    mRecyclerView.clearOnScrollListeners();
                    mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
                        @Override
                        public void onLoadMore(int currentPage) {
                            mTask = mPresenter.nextPage();
                        }
                    });
                }
            }
        }, 5000);
    }

    @Override
    public void onSearchResult(List<BookInfo> books) {
        mAdapter.setBooks(books);
        mAdapter.notifyDataSetChanged();
        if (books.size() > 0) {
            Snackbar.make(mRecyclerView, getString(R.string.founded_entries, books.size()), BaseTransientBottomBar.LENGTH_LONG).show();
            mFabUp.setVisibility(View.VISIBLE);
        } else {
            Snackbar.make(mRecyclerView, R.string.no_related_books, BaseTransientBottomBar.LENGTH_LONG).show();
            mFabUp.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNextPageResult(List<BookInfo> books) {
        mAdapter.addBooks(books);
        mAdapter.notifyDataSetChanged();
        if (books.size() > 0) {
            Snackbar.make(mRecyclerView, getString(R.string.loaded_entries, books.size()), BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRecyclerView, R.string.no_more_results, BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setPresenter(LibSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
