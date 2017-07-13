package cc.metapro.openct.splash.schoolselection

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

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.DBManger
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter
import cc.metapro.openct.utils.base.MyObserver
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.emilsjolander.stickylistheaders.StickyListHeadersListView

class SchoolSelectionActivity : BaseActivity(), SearchView.OnQueryTextListener {

    @BindView(R.id.toolbar)
    internal lateinit var mToolbar: Toolbar
    @BindView(R.id.school_name)
    internal lateinit var mSearchView: SearchView
    @BindView(R.id.school_list_view)
    internal lateinit var mListView: StickyListHeadersListView

    private lateinit var result: String
    private lateinit var mAdapter: SchoolAdapter

    override val presenter: BasePresenter?
        get() = null

    override val layout: Int
        get() = R.layout.activity_school_selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setSupportActionBar(mToolbar)
        result = PrefHelper.getString(this, R.string.pref_school_name, "")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.schools, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.update -> updateSchools(true)
            R.id.report -> Toast.makeText(this, R.string.tmp_add_school_promt, Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        updateSchools(false)
    }

    private fun updateSchools(online: Boolean) {
        ActivityUtils.showProgressDialog(this, R.string.loading_school_list)

        val observable = Observable.create(ObservableOnSubscribe<SchoolAdapter> { e ->
            if (online) {
                // delete current schools
                ActivityUtils.dismissProgressDialog()
                DBManger.updateSchools(this@SchoolSelectionActivity, null)
            }

            mAdapter = SchoolAdapter(this@SchoolSelectionActivity)
            e.onNext(mAdapter)
        })

        val observer = object : MyObserver<SchoolAdapter>(TAG) {
            override fun onNext(t: SchoolAdapter) {
                if (!online) {
                    ActivityUtils.dismissProgressDialog()
                }
                setViews(t)
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    private fun setViews(mAdapter: SchoolAdapter) {
        mListView.adapter = mAdapter
        mListView.setOnItemClickListener { _, _, position, _ ->
            result = mAdapter.getItem(position).toString()
            Snackbar.make(mListView, getString(R.string.selected_school, result), BaseTransientBottomBar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok) {
                        val intent = Intent()
                        intent.putExtra(SCHOOL_RESULT, result)
                        setResult(Activity.RESULT_OK, intent)
                        finish()
                    }.show()
        }
        mListView.requestFocus()

        mSearchView.onActionViewExpanded()
        mSearchView.isSubmitButtonEnabled = true
        mSearchView.setOnQueryTextListener(this)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        mAdapter.setTextFilter(query)
                .subscribeWith(object : MyObserver("") {
                    override fun onNext(o: Any) {
                        mAdapter.notifyDataSetChanged()
                        mListView.smoothScrollToPosition(0)
                    }
                })
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        mAdapter.setTextFilter(newText)
                .subscribeWith(object : MyObserver("") {
                    override fun onNext(o: Any) {
                        mAdapter.notifyDataSetChanged()
                        mListView.smoothScrollToPosition(0)
                    }
                })
        return true
    }

    override fun onDestroy() {
        PrefHelper.putString(this, R.string.pref_school_name, result)
        LocalHelper.needUpdateUniversity()
        super.onDestroy()
    }

    companion object {

        val REQUEST_SCHOOL_NAME = 1
        val SCHOOL_RESULT = "school_name"
        private val TAG = SchoolSelectionActivity::class.java.name
    }
}
