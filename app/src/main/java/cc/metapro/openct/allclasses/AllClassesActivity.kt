package cc.metapro.openct.allclasses

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

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.classdetail.ClassDetailActivity
import cc.metapro.openct.custom.CustomActivity
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.RecyclerViewHelper
import cc.metapro.openct.utils.base.BaseActivity
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener

class AllClassesActivity : BaseActivity(), AllClassesContract.View {
    @BindView(R.id.toolbar)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.recycler_view)
    internal var mRecyclerView: SwipeMenuRecyclerView? = null
    private var mAdapter: AllClassesAdapter? = null
    private var mPresenter: AllClassesContract.Presenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setSupportActionBar(mToolbar)
        AllClassesPresenter(this, this)
    }

    override val layout: Int
        get() = R.layout.activity_all_classes

    override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    override fun setPresenter(presenter: AllClassesContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.classes, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.export_classes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
                } else {
                    mPresenter!!.exportClasses()
                }
            } else {
                mPresenter!!.exportClasses()
            }
        } else if (id == R.id.clear_classes) {
            mPresenter!!.clearClasses()
        } else if (id == R.id.custom) {
            CustomActivity.actionStart(this, Constants.TYPE_CLASS)
        } else if (id == R.id.import_from_excel) {
            mPresenter!!.loadFromExcel(supportFragmentManager)
        } else if (id == R.id.add_class) {
            ClassDetailActivity.actionStart(this, getString(R.string.new_class))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun updateClasses() {
        mAdapter = AllClassesAdapter(this)
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView!!, mAdapter!!)
        mRecyclerView!!.isItemViewSwipeEnabled = true

        mRecyclerView!!.setOnItemMoveListener(object : OnItemMoveListener {
            override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
                return false
            }

            override fun onItemDismiss(position: Int) {
                val toRemove = Constants.sClasses[position]
                Constants.sClasses.removeAt(position)
                mAdapter!!.notifyDataSetChanged()
                val snackbar = Snackbar.make(mRecyclerView!!, toRemove.name + " " + getString(R.string.deleted), BaseTransientBottomBar.LENGTH_INDEFINITE)
                snackbar.setAction(android.R.string.cancel) {
                    Constants.sClasses.add(toRemove)
                    mAdapter!!.notifyDataSetChanged()
                    snackbar.dismiss()
                    Snackbar.make(mRecyclerView!!, toRemove.name + " " + getString(R.string.restored), BaseTransientBottomBar.LENGTH_LONG).show()
                    mRecyclerView!!.smoothScrollToPosition(0)
                }
                snackbar.show()
            }
        })
    }

    override fun onBackPressed() {
        mPresenter!!.storeClasses(Constants.sClasses)
        super.onBackPressed()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mPresenter!!.exportClasses()
            } else {
                Toast.makeText(this, R.string.no_write_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        private val REQUEST_WRITE_STORAGE = 112
    }
}
