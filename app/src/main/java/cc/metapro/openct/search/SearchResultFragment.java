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
import android.support.annotation.Keep;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.EndlessRecyclerOnScrollListener;
import cc.metapro.openct.data.university.item.BookInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;
import io.reactivex.disposables.Disposable;

@Keep
public class SearchResultFragment extends Fragment implements LibSearchContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.fab)
    FloatingActionButton mFabUp;

    @OnClick(R.id.fab)
    public void upToTop() {
        mRecyclerView.smoothScrollToPosition(0);
    }

    private BooksAdapter mAdapter;
    private LibSearchContract.Presenter mPresenter;
    private Disposable mTask;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_result, container, false);
        ButterKnife.bind(this, view);
        mAdapter = new BooksAdapter(getContext());
        mLinearLayoutManager = RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.search();
            }
        });
        return view;
    }

    @Override
    public void onDestroy() {
        if (mTask != null) {
            mTask.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void showOnSearching() {
        mSwipeRefreshLayout.setRefreshing(true);
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
        mSwipeRefreshLayout.setRefreshing(false);
        if (books.size() > 0) {
            Snackbar.make(mRecyclerView, "找到了 " + books.size() + " 条结果", BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRecyclerView, R.string.no_related_books, BaseTransientBottomBar.LENGTH_LONG).show();
            mFabUp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNextPageResult(List<BookInfo> infos) {
        mAdapter.addBooks(infos);
        mAdapter.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
        if (infos.size() > 0) {
            Snackbar.make(mRecyclerView, "加载了 " + infos.size() + " 条结果", BaseTransientBottomBar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mRecyclerView, R.string.no_more_results, BaseTransientBottomBar.LENGTH_LONG).show();
        }
    }

    @Override
    public void setPresenter(LibSearchContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
