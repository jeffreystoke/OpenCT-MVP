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

import android.os.Bundle
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
import cc.metapro.openct.R
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter
import com.blankj.utilcode.util.ToastUtils
import kotlinx.android.synthetic.main.activity_school_selection.*

class SchoolSelectionActivity : BaseActivity(), SearchView.OnQueryTextListener {

    private lateinit var result: String

    override val presenter: BasePresenter?
        get() = null

    override val layout: Int
        get() = R.layout.activity_school_selection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
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
            R.id.report -> ToastUtils.showLong(R.string.tmp_add_school_promt)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateSchools(online: Boolean) {

    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return true
    }

    companion object {
        val REQUEST_SCHOOL_NAME = 1
        val SCHOOL_RESULT = "school_name"
    }
}
