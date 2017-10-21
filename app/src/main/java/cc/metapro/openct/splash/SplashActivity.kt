package cc.metapro.openct.splash

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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import cc.metapro.openct.R
import cc.metapro.openct.myclass.ClassActivity
import cc.metapro.openct.splash.views.LoginFragment
import cc.metapro.openct.splash.views.SchoolFragment
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter

class SplashActivity : BaseActivity() {

    private lateinit var mViewPager: ViewPager
    private val mSchoolFragment: SchoolFragment = SchoolFragment.instance
    private val mCmsFragment: LoginFragment = LoginFragment.getInstance(Constants.TYPE_CMS)
    private val mLibFragment: LoginFragment = LoginFragment.getInstance(Constants.TYPE_LIB)

    private var misScrolled = false

    override val presenter: BasePresenter?
        get() = null

    override val layout: Int
        get() = R.layout.activity_splash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PrefHelper.getBoolean(this, R.string.pref_initialed, false)) {
            startActivity(Intent(this, ClassActivity::class.java))
            finish()
            return
        }

        PrefHelper.putBoolean(this, R.string.pref_initialed, true)
        setViewPager()
    }

    private fun setViewPager() {
        SplashPresenter(mSchoolFragment, mCmsFragment, mLibFragment)
        val pagerAdapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                when (position) {
                    0 -> return mSchoolFragment
                    1 -> return mCmsFragment
                    2 -> return mLibFragment
                }
                throw IndexOutOfBoundsException("three fragments at most!")
            }

            override fun getCount(): Int {
                return 3
            }
        }

        mViewPager.adapter = pagerAdapter
        mViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {
                when (state) {
                    ViewPager.SCROLL_STATE_IDLE -> {
                        if (mViewPager.currentItem == pagerAdapter.count - 1 && !misScrolled) {
                            pagerAdapter.getItem(pagerAdapter.count - 1).userVisibleHint = false
                            startActivity(Intent(this@SplashActivity, ClassActivity::class.java))
                            finish()
                        }
                        misScrolled = false
                    }
                    ViewPager.SCROLL_STATE_DRAGGING -> misScrolled = false
                    ViewPager.SCROLL_STATE_SETTLING -> misScrolled = true
                }
            }
        })
    }
}
