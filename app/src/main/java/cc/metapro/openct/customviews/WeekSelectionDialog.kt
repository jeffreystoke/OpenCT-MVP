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
import android.support.v7.app.AlertDialog
import cc.metapro.openct.R
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.base.BaseDialog
import cc.metapro.openct.widget.DailyClassWidget


class WeekSelectionDialog : BaseDialog() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val currentWeek = Integer.parseInt(PrefHelper.getString(activity, R.string.pref_current_week, "1"))

        return AlertDialog.Builder(activity)
                .setTitle(R.string.select_week)
                .setSingleChoiceItems(R.array.pref_week_seq_keys, currentWeek - 1
                ) { dialog, which ->
                    val selectedWeek = which + 1
                    PrefHelper.putString(activity, R.string.pref_current_week, selectedWeek.toString() + "")
                    sCallBack!!.onSelection(selectedWeek)
                    DailyClassWidget.update(activity)
                    dismiss()
                }
                .create()
    }

    override fun onDestroy() {
        super.onDestroy()
        sCallBack = null
    }

    interface SelectionCallback {
        fun onSelection(index: Int)
    }

    companion object {

        private var sCallBack: SelectionCallback? = null

        fun newInstance(callback: SelectionCallback): WeekSelectionDialog {
            sCallBack = callback
            return WeekSelectionDialog()
        }
    }
}
