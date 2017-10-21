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
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import cc.metapro.openct.R
import cc.metapro.openct.utils.Constants
import org.jsoup.nodes.Element
import java.util.*

class TableSettingDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(getString(R.string.what_is, mContents!![mIndex++]))
                .setMultiChoiceItems(mShowingOptions!!.toTypedArray(), null) { _, which, isChecked ->
                    val s = mShowingOptions!![which]
                    if (isChecked) {
                        mResult!!.put(s, mIndex - 1)
                    } else {
                        mResult!!.remove(s)
                    }
                }
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    mShowingOptions = ArrayList<String>()
                    for (s in mOptions!!) {
                        if (!mResult!!.containsKey(s)) {
                            mShowingOptions!!.add(s)
                        }
                    }

                    if (mShowingOptions!!.size > 0 && mIndex < mContents!!.size) {
                        TableSettingDialog().show(fragmentManager, "table_setting")
                    } else {
                        mCallBack!!.onResult(mResult!!)

                        mContents = null
                        mOptions = null
                        mCallBack = null
                        mResult = null
                    }
                }
                .create()
    }

    interface TableSettingCallBack {
        fun onResult(indexMap: Map<String, Int>)
    }

    companion object {

        var mOptions: MutableList<String>? = null
        private var mContents: List<String>? = null
        private var mIndex: Int = 0
        private var mShowingOptions: MutableList<String>? = null

        private var mCallBack: TableSettingCallBack? = null
        private var mResult: MutableMap<String, Int>? = null

        fun newInstance(rawInfoList: List<Element>, callBack: TableSettingCallBack?): TableSettingDialog {
            val element: Element? = rawInfoList.firstOrNull { it.text().length > 10 }

            if (element != null) {
                val defaultTitles = arrayOf(Constants.NAME, Constants.TIME, Constants.DURING, Constants.TYPE, Constants.PLACE, Constants.TEACHER)
                mOptions = MutableList(defaultTitles.size, fun(i: Int): String {
                    return defaultTitles[i]!!
                })
                mShowingOptions = mOptions
                mIndex = 0
//                val sample = element.text().split((HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
//                val s = sample.split(HTMLUtils.BR_REPLACER.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
//                mResult = HashMap<String, Int>()
//                mContents = ArrayList(Arrays.asList(*s))
//                mCallBack = callBack
            } else {
                throw NullPointerException("Can't find sample element")
            }

            return TableSettingDialog()
        }
    }
}
