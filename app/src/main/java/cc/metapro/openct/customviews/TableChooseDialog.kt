package cc.metapro.openct.customviews

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

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import cc.metapro.openct.R
import cc.metapro.openct.data.university.ClassTableInfo
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.LoginPresenter
import cc.metapro.openct.utils.webutils.TableUtils
import kotlinx.android.synthetic.main.dialog_table_choose.*
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

class TableChooseDialog : DialogFragment() {
    private var mTableIds: MutableList<String>? = null

    private fun generateClassTableInfo(baseInfo: ClassTableInfo?, indexMap: Map<String, Int>): ClassTableInfo {
        var baseInfo = baseInfo
        if (baseInfo == null) {
            baseInfo = ClassTableInfo()
        }

        try {
            baseInfo.mNameIndex = indexMap[Constants.NAME]!!
        } catch (e: Exception) {
            baseInfo.mNameIndex = 0
        }

        try {
            baseInfo.mTimeIndex = indexMap[Constants.TIME]!!
        } catch (e: Exception) {
            baseInfo.mTimeIndex = 0
        }

        try {
            baseInfo.mDuringIndex = indexMap[Constants.DURING]!!
        } catch (e: Exception) {
            baseInfo.mDuringIndex = 0
        }

        try {
            baseInfo.mPlaceIndex = indexMap[Constants.PLACE]!!
        } catch (e: Exception) {
            baseInfo.mPlaceIndex = 0
        }

        try {
            baseInfo.mTeacherIndex = indexMap[Constants.TEACHER]!!
        } catch (e: Exception) {
            baseInfo.mTeacherIndex = 0
        }

        try {
            baseInfo.mTypeIndex = indexMap[Constants.TYPE]!!
        } catch (e: Exception) {
            baseInfo.mTypeIndex = 0
        }

        return baseInfo
    }

    fun select() {
        if (!mTableIds!!.isEmpty()) {
            val tableId = mTableIds!![view_pager!!.currentItem]
            val context = activity
            val targetTable = tableMap!![tableId]
            when (TYPE) {
//                Constants.TYPE_CLASS -> {
//                    val rawInfoList = UniversityUtils.getRawClasses(targetTable, activity)
//                    try {
//                        class c : TableSettingDialog.TableSettingCallBack {
//                            override fun onResult(indexMap: Map<String, Int>) {
//                                val info = generateClassTableInfo(Constants.sDetailCustomInfo.mClassTableInfo, indexMap as Map<String, Int>)
//                                info.mClassTableID = tableId
//                                val classes = UniversityUtils.generateClasses(context, rawInfoList, info)
//                                Constants.sDetailCustomInfo.setClassTableInfo(info)
//
//                                manger.updateAdvCustomInfo(Constants.sDetailCustomInfo)
//                                manger.updateClasses(classes)
//                                if (mPresenter is ClassContract.Presenter) {
//                                    (mPresenter as ClassContract.Presenter).loadLocalClasses()
//                                }
//                                Toast.makeText(context, R.string.custom_finish_tip, Toast.LENGTH_LONG).show()
//                            }
//
//                        }
//                        TableSettingDialog.newInstance(rawInfoList, c()).show(fragmentManager, "setting_dialog")
//                        dismiss()
//                    } catch (e: Exception) {
//                        Toast.makeText(context, R.string.sorry_for_unable_to_get_class_info, Toast.LENGTH_LONG).show()
//                    }
//
//                }
//                Constants.TYPE_GRADE -> {
//                    Constants.sDetailCustomInfo.gradeTableId = tableId
//                    manger.updateAdvCustomInfo(Constants.sDetailCustomInfo)
//                    manger.updateGrades(UniversityUtils.generateInfoFromTable(targetTable, GradeInfo::class.java))
//                    if (mPresenter is GradeContract.Presenter) {
//                        (mPresenter as GradeContract.Presenter).loadLocalGrades()
//                    }
//                    dismiss()
//                }
//                Constants.TYPE_SEARCH -> {
//                }
//                Constants.TYPE_BORROW -> {
//                    Constants.sDetailCustomInfo.borrowTableId = tableId
//                    manger.updateAdvCustomInfo(Constants.sDetailCustomInfo)
//                    manger.updateBorrows(UniversityUtils.generateInfoFromTable(targetTable, BorrowInfo::class.java))
//                    if (mPresenter != null && mPresenter is BorrowContract.Presenter) {
//                        (mPresenter as BorrowContract.Presenter).loadLocalBorrows()
//                    }
//                    dismiss()
//                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_table_choose, null)
        setView()

        Constants.checkAdvCustomInfo(activity)
        return AlertDialog.Builder(activity)
                .setView(view)
                .setTitle(R.string.swipe_choose_table)
                .setPositiveButton(android.R.string.ok) { dialog, which -> select() }
                .create()
    }

    private fun setView() {
        mTableIds = ArrayList<String>(tableMap!!.size)
        for (s in tableMap!!.keys) {
            mTableIds!!.add(s)
        }

        val views = ArrayList<View>(mTableIds!!.size)
        for (s in mTableIds!!) {
            val textView = TextView(activity)

            val content = tableMap!![s].toString()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
            } else {
                textView.text = Html.fromHtml(content)
            }
            views.add(textView)
        }

//        mTabBar!!.setupWithViewPager(mViewPager)
//        mViewPager!!.adapter = object : PagerAdapter() {
//            override fun getCount(): Int {
//                return mTableIds!!.size
//            }
//
//            override fun getPageTitle(position: Int): CharSequence {
//                return mTableIds!![position]
//            }
//
//            override fun isViewFromObject(view: View, `object`: Any): Boolean {
//                return view === `object`
//            }
//
//            override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//                container.removeView(views[position])
//            }
//
//            override fun instantiateItem(container: ViewGroup, position: Int): Any {
//                container.addView(views[position])
//                return views[position]
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
        TYPE = null
        tableMap = null
        mTableIds = null
    }

    companion object {

        private var TYPE: String? = null
        private var tableMap: Map<String, Element>? = null
        private var mPresenter: LoginPresenter? = null

        fun newInstance(type: String, source: Document, presenter: LoginPresenter?): TableChooseDialog {
            tableMap = TableUtils.getTablesFromTargetPage(source)
            TYPE = type
            mPresenter = presenter
            return TableChooseDialog()
        }
    }
}
