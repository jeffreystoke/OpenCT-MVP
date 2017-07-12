package cc.metapro.openct.grades.cet

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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.TextView

import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.utils.base.BaseDialog


class CETResultDialog : BaseDialog() {
    @BindView(R.id.full_name)
    internal var mFullName: TextView? = null
    @BindView(R.id.school)
    internal var mSchool: TextView? = null
    @BindView(R.id.type)
    internal var mType: TextView? = null
    @BindView(R.id.ticket_num)
    internal var mTicketNum: TextView? = null
    @BindView(R.id.time)
    internal var mTime: TextView? = null
    @BindView(R.id.grade)
    internal var mGrade: TextView? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.dialog_cet_result, null)
        ButterKnife.bind(this, view)
        setInfo()
        return AlertDialog.Builder(activity)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create()
    }

    private fun setInfo() {
        val fullName = result!![getString(R.string.key_full_name)]
        if (!TextUtils.isEmpty(fullName)) {
            mFullName!!.visibility = View.VISIBLE
            mFullName!!.text = getString(R.string.full_name) + ":\t" + fullName
        }

        val school = result!![getString(R.string.key_school)]
        if (!TextUtils.isEmpty(school)) {
            mSchool!!.visibility = View.VISIBLE
            mSchool!!.text = getString(R.string.school) + ":\t" + school
        }

        val type = result!![getString(R.string.key_cet_type)]
        if (!TextUtils.isEmpty(type)) {
            mType!!.visibility = View.VISIBLE
            mType!!.text = getString(R.string.cet_type) + ":\t" + type
        }

        val ticketNum = result!![getString(R.string.key_ticket_num)]
        if (!TextUtils.isEmpty(ticketNum)) {
            mTicketNum!!.visibility = View.VISIBLE
            mTicketNum!!.text = getString(R.string.ticket_number) + ":\t" + ticketNum
        }

        val time = result!![getString(R.string.key_cet_time)]
        if (!TextUtils.isEmpty(time)) {
            mTime!!.visibility = View.VISIBLE
            mTime!!.text = getString(R.string.exam_time) + time
        }

        val grade = result!![getString(R.string.key_cet_grade)]
        if (!TextUtils.isEmpty(grade)) {
            mGrade!!.visibility = View.VISIBLE
            mGrade!!.text = getString(R.string.grades) + ":\t" + grade
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        result = null
    }

    companion object {

        private var result: Map<String, String>? = null

        fun newInstance(resultMap: Map<String, String>): CETResultDialog {
            result = resultMap
            return CETResultDialog()
        }
    }
}
