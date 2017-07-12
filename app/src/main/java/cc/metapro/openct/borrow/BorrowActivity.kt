package cc.metapro.openct.borrow

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

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.data.university.model.BorrowInfo
import cc.metapro.openct.pref.SettingsActivity
import cc.metapro.openct.utils.RecyclerViewHelper
import cc.metapro.openct.utils.base.BaseActivity

class BorrowActivity : BaseActivity(), BorrowContract.View {

    @BindView(R.id.toolbar)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.fab)
    internal var mFab: FloatingActionButton? = null
    @BindView(R.id.recycler_view)
    internal var mRecyclerView: RecyclerView? = null
    @BindView(R.id.image)
    internal var mImage: ImageView? = null

    private var mPresenter: BorrowContract.Presenter? = null
    private var mBorrowAdapter: BorrowAdapter? = null

    @OnClick(R.id.fab)
    fun load() {
        val user = LocalHelper.getLibStuInfo(this)
        if (user.isEmpty) {
            Toast.makeText(this, R.string.please_fill_lib_info, Toast.LENGTH_LONG).show()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } else {
            mPresenter!!.loadOnlineInfo(supportFragmentManager)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        // set toolbar
        mToolbar!!.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter)
        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mBorrowAdapter = BorrowAdapter(this)
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView!!, mBorrowAdapter!!)
        BorrowPresenter(this, this)
    }

    override val layout: Int
        get() = R.layout.activity_borrow

    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    override fun setPresenter(presenter: BorrowContract.Presenter) {
        mPresenter = presenter
    }

    override fun updateBorrows(borrows: List<BorrowInfo>) {
        if (borrows.isEmpty()) {
            Snackbar.make(mRecyclerView!!, R.string.no_borrows, BaseTransientBottomBar.LENGTH_LONG).show()
        } else {
            mBorrowAdapter!!.setNewBorrows(borrows)
            mBorrowAdapter!!.notifyDataSetChanged()
            Snackbar.make(mRecyclerView!!, getString(R.string.borrow_entries, borrows.size), BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_all -> mPresenter!!.showAll()
            R.id.show_due -> mPresenter!!.showDue()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.borrow, menu)
        return true
    }
}