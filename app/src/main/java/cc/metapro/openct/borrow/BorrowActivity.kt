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

import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.view.Menu
import android.view.MenuItem
import cc.metapro.openct.R
import cc.metapro.openct.data.source.LocalHelper
import cc.metapro.openct.data.university.model.BorrowInfo
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter
import cc.metapro.openct.utils.setRecyclerView
import kotlinx.android.synthetic.main.activity_borrow.*

class BorrowActivity : BaseActivity(), BorrowContract.View {

    private lateinit var mBorrowAdapter: BorrowAdapter
    private lateinit var mBorrowPresenter: BorrowContract.Presenter

    override val presenter: BasePresenter
        get() = mBorrowPresenter

    override val layout: Int
        get() = R.layout.activity_borrow

    fun load() {
        val user = LocalHelper.getLibStuInfo(this)
//        if (user.isEmpty) {
//            Toast.makeText(this, R.string.please_fill_lib_info, Toast.LENGTH_LONG).show()
//            val intent = Intent(this, SettingsActivity::class.java)
//            startActivity(intent)
//        } else {
//            mBorrowPresenter.loadOnlineInfo(supportFragmentManager)
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // set toolbar
//        mToolbar.overflowIcon = ContextCompat.getDrawable(this, R.drawable.ic_filter)
        setSupportActionBar(toolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        mBorrowAdapter = BorrowAdapter(this)
        setRecyclerView(this, recycler_view, mBorrowAdapter)
        BorrowPresenter(this)
    }

    override fun setPresenter(p: BorrowContract.Presenter) {
        mBorrowPresenter = p
    }

    override fun updateBorrows(l: List<BorrowInfo>) {
        if (l.isEmpty()) {
            Snackbar.make(recycler_view, R.string.no_borrows, BaseTransientBottomBar.LENGTH_LONG).show()
        } else {
            mBorrowAdapter.setNewBorrows(l)
            mBorrowAdapter.notifyDataSetChanged()
            Snackbar.make(recycler_view, getString(R.string.borrow_entries, l.size), BaseTransientBottomBar.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.show_all -> mBorrowPresenter.showAll()
            R.id.show_due -> mBorrowPresenter.showDue()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.borrow, menu)
        return true
    }
}