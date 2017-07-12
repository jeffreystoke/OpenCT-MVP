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

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AlertDialog
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.grades.GradeContract
import cc.metapro.openct.utils.base.BaseDialog
import com.rengwuxian.materialedittext.MaterialEditText
import java.util.*

class CETQueryDialog : BaseDialog() {
    internal lateinit var mPreferences: SharedPreferences
    @BindView(R.id.ticket_num)
    internal var mNum: MaterialEditText? = null
    @BindView(R.id.full_name)
    internal var mName: MaterialEditText? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = activity.layoutInflater.inflate(R.layout.dialog_cet_query, null)
        ButterKnife.bind(this, view)
        val alertDialog = AlertDialog.Builder(activity)
                .setTitle(R.string.cet_query)
                .setView(view)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val queryMap = HashMap<String, String>(2)
                    queryMap.put(getString(R.string.key_ticket_num), mNum!!.text.toString())
                    queryMap.put(getString(R.string.key_full_name), mName!!.text.toString())
                    mPresenter!!.loadCETGrade(queryMap)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.preserve) { dialog, which ->
                    val editor = mPreferences.edit()
                    editor.putString(getString(R.string.pref_cet_ticket_num), mNum!!.text.toString())
                    editor.putString(getString(R.string.pref_cet_full_name), mName!!.text.toString())
                    editor.apply()
                    Toast.makeText(context, R.string.saved, Toast.LENGTH_SHORT).show()
                }.create()

        mPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        mNum!!.setText(mPreferences.getString(getString(R.string.pref_cet_ticket_num), ""))
        mName!!.setText(mPreferences.getString(getString(R.string.pref_cet_full_name), ""))
        return alertDialog
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter = null
    }

    companion object {

        private var mPresenter: GradeContract.Presenter? = null

        fun newInstance(presenter: GradeContract.Presenter): CETQueryDialog {
            mPresenter = presenter
            return CETQueryDialog()
        }
    }
}
