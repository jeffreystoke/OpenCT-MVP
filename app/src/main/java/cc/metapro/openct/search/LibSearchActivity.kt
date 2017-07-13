package cc.metapro.openct.search

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

import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v7.widget.AppCompatSpinner
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView

import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.OnEditorAction
import cc.metapro.openct.R
import cc.metapro.openct.customviews.EndlessRecyclerOnScrollListener
import cc.metapro.openct.data.university.model.BookInfo
import cc.metapro.openct.utils.RecyclerViewHelper
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter
import io.reactivex.disposables.Disposable

class LibSearchActivity : BaseActivity(), LibSearchContract.View {
    @BindView(R.id.toolbar)
    internal lateinit var mToolbar: Toolbar
    @BindView(R.id.fab)
    internal lateinit var mFabSearch: FloatingActionButton
    @BindView(R.id.lib_search_content_edittext)
    internal lateinit var mEditText: EditText
    @BindView(R.id.type_spinner)
    internal lateinit var mSpinner: AppCompatSpinner
    @BindView(R.id.recycler_view)
    internal lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.fab_up)
    internal lateinit var mFabUp: FloatingActionButton
    @BindView(R.id.image)
    internal lateinit var mImage: ImageView

    private lateinit var mPresenter: LibSearchContract.Presenter
    private lateinit var mAdapter: BooksAdapter
    private lateinit var mTask: Disposable
    private lateinit var mLinearLayoutManager: LinearLayoutManager

    override val presenter: BasePresenter?
        get() = mPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        // set mToolbar
        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)

        mAdapter = BooksAdapter(this)
        val adapter = ArrayAdapter(this, R.layout.item_search_type, resources.getStringArray(R.array.lib_search_type))
        mSpinner.adapter = adapter
        mLinearLayoutManager = RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mAdapter)
        mEditText.requestFocus()

        // TODO: 17/4/12 load image
        LibSearchPresenter(this, this)
    }

    override val layout: Int
        get() = R.layout.activity_search

    @OnClick(R.id.fab_up)
    fun upToTop() {
        mRecyclerView.smoothScrollToPosition(0)
    }

    @OnClick(R.id.fab)
    fun fabSearch() {
        mTask = mPresenter.search(mSpinner.selectedItem.toString(), mEditText.text.toString())
    }

    @OnEditorAction(R.id.lib_search_content_edittext)
    fun editorSearch(i: Int, keyEvent: KeyEvent?): Boolean {
        if (i == EditorInfo.IME_ACTION_SEARCH || keyEvent != null && keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
            mTask = mPresenter.search(mSpinner.selectedItem.toString(), mEditText.text.toString())
            return true
        }
        return false
    }

    override fun onDestroy() {
        mTask.dispose()
        super.onDestroy()
    }

    override fun showOnSearching() {
        mRecyclerView.postDelayed({
            mRecyclerView.clearOnScrollListeners()
            mRecyclerView.addOnScrollListener(object : EndlessRecyclerOnScrollListener(mLinearLayoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    mTask = mPresenter.nextPage()
                }
            })
        }, 5000)
    }

    override fun onSearchResult(books: List<BookInfo>) {
        mAdapter.setBooks(books.toMutableList())
        mAdapter.notifyDataSetChanged()
        if (books.isNotEmpty()) {
            Snackbar.make(mRecyclerView, getString(R.string.founded_entries, books.size), BaseTransientBottomBar.LENGTH_LONG).show()
            mFabUp.visibility = View.VISIBLE
        } else {
            Snackbar.make(mRecyclerView, R.string.no_related_books, BaseTransientBottomBar.LENGTH_LONG).show()
            mFabUp.visibility = View.GONE
        }
    }

    override fun onNextPageResult(books: List<BookInfo>) {
        mAdapter.addBooks(books)
        mAdapter.notifyDataSetChanged()
        if (books.isNotEmpty()) {
            Snackbar.make(mRecyclerView, getString(R.string.loaded_entries, books.size), BaseTransientBottomBar.LENGTH_LONG).show()
        } else {
            Snackbar.make(mRecyclerView, R.string.no_more_results, BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    override fun setPresenter(p: LibSearchContract.Presenter) {
        mPresenter = p
    }
}
