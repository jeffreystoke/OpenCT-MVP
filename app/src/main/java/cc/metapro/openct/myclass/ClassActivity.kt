package cc.metapro.openct.myclass

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
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.ColorInt
import android.support.design.widget.NavigationView
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.content.ContextCompat
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.allclasses.AllClassesActivity
import cc.metapro.openct.borrow.BorrowActivity
import cc.metapro.openct.customviews.WeekSelectionDialog
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.grades.GradeActivity
import cc.metapro.openct.pref.SettingsActivity
import cc.metapro.openct.search.LibSearchActivity
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.ReferenceUtils
import cc.metapro.openct.utils.base.BaseActivity
import com.bumptech.glide.Glide
import com.jrummyapps.android.colorpicker.ColorPickerDialog
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener
import com.jrummyapps.android.colorpicker.ColorShape

class ClassActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener, ClassContract.View {

    @BindView(R.id.toolbar)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.drawer_layout)
    internal var mDrawerLayout: DrawerLayout? = null
    @BindView(R.id.nav_view)
    internal var mNavigationView: NavigationView? = null
    @BindView(R.id.tab_layout)
    internal var mTabLayout: TabLayout? = null
    @BindView(R.id.view_pager)
    internal var mViewPager: ViewPager? = null
    @BindView(R.id.main_background)
    internal var mBackground: ImageView? = null

    internal lateinit var mPresenter: ClassContract.Presenter
    private var mExitState: Boolean = false
    private var mPagerAdapter: FragmentPagerAdapter? = null
    private var mDailyFragment: DailyFragment? = null
    private var mTableFragment: TableFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        setSupportActionBar(mToolbar)
        mExitState = false

        val toggle = ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        toggle.syncState()

        mNavigationView!!.setNavigationItemSelectedListener(this)
        initViewPager()
        mPresenter = ClassPresenter(this, this)
    }

    override val layout: Int
        get() = R.layout.activity_main

    private fun initViewPager() {
        mDailyFragment = DailyFragment.newInstance()
        mTableFragment = TableFragment.newInstance()

        mPagerAdapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment? {
                when (position) {
                    0 -> return mDailyFragment
                    1 -> return mTableFragment
                }
                return null
            }

            override fun getCount(): Int {
                return 2
            }

            override fun getPageTitle(position: Int): CharSequence {
                when (position) {
                    0 -> return getString(R.string.text_daily_classes)
                    1 -> return getString(R.string.text_current_week, LocalHelper.getCurrentWeek(this@ClassActivity))
                }
                return super.getPageTitle(position)
            }
        }
        mViewPager!!.adapter = mPagerAdapter

        mTabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}

            override fun onTabUnselected(tab: TabLayout.Tab) {}

            override fun onTabReselected(tab: TabLayout.Tab) {
                if (tab == mTabLayout!!.getTabAt(1)) {
                    class selectionCallback : WeekSelectionDialog.SelectionCallback {
                        override fun onSelection(index: Int) {
                            showClasses(Constants.sClasses, index)
                            val tab1 = mTabLayout!!.getTabAt(1)
                            if (tab1 != null) {
                                tab1.text = getString(R.string.text_current_week, index)
                            }
                        }
                    }

                    val x = selectionCallback()
                    WeekSelectionDialog.newInstance(x).show(supportFragmentManager, "week_selection")
                }
            }
        })
        mTabLayout!!.setupWithViewPager(mViewPager)

        var index = Integer.parseInt(PrefHelper.getString(this, R.string.pref_homepage_selection, "0"))
        if (index > 1) {
            index = 0
            PrefHelper.putString(this, R.string.pref_homepage_selection, "0")
        }
        mViewPager!!.currentItem = index
    }

    override fun showClasses(classes: Classes, week: Int) {
        mTableFragment!!.showClasses(classes, week)
        mDailyFragment!!.showClasses(classes, week)
    }

    override fun setPresenter(presenter: ClassContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.home, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.refresh_classes) {
            val user = LocalHelper.getCmsStuInfo(this)
            if (user.isEmpty) {
                Toast.makeText(this, R.string.please_fill_cms_info, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, SettingsActivity::class.java))
            } else {
                mPresenter.loadOnlineInfo(supportFragmentManager)
            }
            return true
        } else if (id == R.id.edit_classes) {
            startActivity(Intent(this, AllClassesActivity::class.java))
        } else if (id == R.id.set_background) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val hasPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_STORAGE)
                } else {
                    showFilerChooser()
                }
            } else {
                showFilerChooser()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_grades -> startActivity(Intent(this, GradeActivity::class.java))
            R.id.nav_search -> startActivity(Intent(this, LibSearchActivity::class.java))
            R.id.nav_borrow_info -> startActivity(Intent(this, BorrowActivity::class.java))
            R.id.nav_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.nav_theme -> showThemePicker()
        }
        mDrawerLayout!!.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showThemePicker() {
        val typedValue = TypedValue()
        val theme = theme
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        val dialog = ColorPickerDialog.newBuilder()
                .setDialogTitle(R.string.select_theme)
                .setColorShape(ColorShape.CIRCLE)
                .setAllowCustom(false)
                .setAllowPresets(false)
                .setDialogType(ColorPickerDialog.TYPE_PRESETS)
                .setShowAlphaSlider(false)
                .setShowColorShades(false)
                .setPresets(resources.getIntArray(R.array.theme_color))
                .setColor(ReferenceUtils.getThemeColor(this, R.attr.colorPrimary))
                .create()

        val oldTheme = PrefHelper.getInt(this, R.string.pref_theme_activity, R.style.AppTheme)
        dialog.setColorPickerDialogListener(object : ColorPickerDialogListener {
            override fun onColorSelected(i: Int, @ColorInt i1: Int) {
                PrefHelper.putInt(this@ClassActivity, R.string.pref_theme_activity, getThemeByColor(i1))
            }

            override fun onDialogDismissed(i: Int) {
                val newTheme = PrefHelper.getInt(this@ClassActivity, R.string.pref_theme_activity, R.style.AppTheme)
                if (oldTheme != newTheme) {
                    finish()
                    startActivity(intent)
                }
            }
        })
        dialog.show(fragmentManager, "color_picker")
    }

    override fun onResume() {
        super.onResume()
        val bgUri = PrefHelper.getString(this, R.string.pref_background, "")
        if (!TextUtils.isEmpty(bgUri)) {
            Glide.with(this).load(bgUri).centerCrop().into(mBackground!!)
        }
        mPresenter.start()
        mPagerAdapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            if (mExitState) {
                finish()
            } else {
                Toast.makeText(this, R.string.one_more_press_to_exit, Toast.LENGTH_SHORT).show()
                mExitState = true
                Handler().postDelayed({ mExitState = false }, 2000)
            }
        }
    }

    private fun showFilerChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_schedule_file)), FILE_SELECT_CODE)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this, R.string.fail_file_chooser, Toast.LENGTH_LONG).show()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_SELECT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            val uri = data.data
            PrefHelper.putString(this, R.string.pref_background, uri.toString())
            Glide.with(this).load(uri).centerCrop().into(mBackground!!)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFilerChooser()
            } else {
                Toast.makeText(this, R.string.no_write_permission, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {

        private val FILE_SELECT_CODE = 101
        private val REQUEST_WRITE_STORAGE = 112
    }
}
