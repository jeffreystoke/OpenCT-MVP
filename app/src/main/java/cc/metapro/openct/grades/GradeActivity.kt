package cc.metapro.openct.grades

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
import android.support.design.widget.FloatingActionButton
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
import cc.metapro.openct.custom.CustomActivity
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.data.university.model.GradeInfo
import cc.metapro.openct.grades.cet.CETQueryDialog
import cc.metapro.openct.grades.cet.CETResultDialog
import cc.metapro.openct.pref.SettingsActivity
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.RecyclerViewHelper
import java.util.*

class GradeActivity : BaseActivity(), GradeContract.View {

    @BindView(R.id.recycler_view)
    internal var mRecyclerView: RecyclerView? = null
    @BindView(R.id.fab)
    internal var fab: FloatingActionButton? = null
    @BindView(R.id.toolbar)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.image)
    internal var mImage: ImageView? = null

    private var mPresenter: GradeContract.Presenter? = null
    private var mGradeAdapter: GradeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)

        mGradeAdapter = GradeAdapter(this)
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView!!, mGradeAdapter!!)
        GradePresenter(this, this)
    }

    protected override val layout: Int
        get() = R.layout.activity_grade

    @OnClick(R.id.fab)
    fun refresh() {
        val user = LocalHelper.getCmsStuInfo(this)
        if (user.isEmpty) {
            Toast.makeText(this, R.string.please_fill_cms_info, Toast.LENGTH_LONG).show()
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        } else {
            mPresenter!!.loadOnlineInfo(supportFragmentManager)
        }
    }

    public override fun onResume() {
        super.onResume()
        mPresenter!!.start()
    }

    override fun onLoadGrades(grades: List<GradeInfo>) {
        mGradeAdapter!!.updateGrades(grades)
        mGradeAdapter!!.notifyDataSetChanged()
    }

    override fun showCETDialog() {
        CETQueryDialog.newInstance(mPresenter!!)
                .show(supportFragmentManager, "cet_query")
    }

    override fun onLoadCETGrade(resultMap: Map<String, String>) {
        CETResultDialog.newInstance(resultMap)
                .show(supportFragmentManager, "cet_result")
    }

    override fun setPresenter(p: GradeContract.Presenter) {
        mPresenter = p
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.grade, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.clear) {
            onLoadGrades(ArrayList<GradeInfo>(0))
            mPresenter!!.clearGrades()
        } else if (id == R.id.query) {
            showCETDialog()
        } else if (id == R.id.custom) {
            CustomActivity.actionStart(this, Constants.TYPE_GRADE)
        }
        return super.onOptionsItemSelected(item)
    }
}
