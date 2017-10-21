package cc.metapro.openct.splash.views


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
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import cc.metapro.openct.R
import cc.metapro.openct.splash.SplashContract
import cc.metapro.openct.splash.schoolselection.SchoolSelectionActivity

class SchoolFragment : Fragment(), SplashContract.SchoolView {

    private var mSelection: TextView? = null
    private var mWeek: Spinner? = null

    private var mPresenter: SplashContract.Presenter? = null

    private var initialed = false

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_school, container, false)
        initialed = true
        return view
    }

    fun onClick() {
        startActivityForResult(
                Intent(context, SchoolSelectionActivity::class.java),
                SchoolSelectionActivity.REQUEST_SCHOOL_NAME)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (!isVisibleToUser && initialed) {
            val week = mWeek!!.selectedItemPosition + 1
            mPresenter!!.setSelectedWeek(week)
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                mPresenter!!.setSelectedSchool(data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT))
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun setPresenter(p: SplashContract.Presenter) {
        mPresenter = p
    }

    override fun showSelectedSchool(name: String) {
        mSelection!!.text = name
    }

    companion object {

        val instance: SchoolFragment
            get() = SchoolFragment()
    }
}
