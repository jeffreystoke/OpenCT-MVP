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
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.BaseDialog
import cc.metapro.openct.utils.base.LoginPresenter
import cc.metapro.openct.utils.webutils.Form
import cc.metapro.openct.utils.webutils.FormHandler
import cc.metapro.openct.utils.webutils.FormUtils
import com.rengwuxian.materialedittext.MaterialEditText
import org.jsoup.nodes.Document
import java.util.*
import java.util.regex.Pattern

class FormDialog : BaseDialog() {
    @BindView(R.id.content)
    internal var mBaseLinearLayout: LinearLayout? = null
    private var mForm: Form? = null
    private var selectionChanged: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val handler = FormHandler(document!!)
        if (Constants.QZDATASOFT.equals(LocalHelper.getUniversity(activity).cmsSys, ignoreCase = true)) {
            mForm = handler.getForm(1)
            if (mForm == null) {
                mForm = handler.getForm(0)
            }
        } else {
            mForm = handler.getForm(0)
        }
        var view: View? = null
        try {
            view = FormUtils.getFormView(context, null!!, mForm!!)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(activity, R.string.can_not_load_prev_target, Toast.LENGTH_LONG).show()
            dismiss()
            return AlertDialog.Builder(activity).create()
        }

        ButterKnife.bind(this, view!!)

        val builder = ActivityUtils
                .getAlertBuilder(activity, R.string.query_what)
                .setPositiveButton(android.R.string.ok) { dialog, which ->
                    val map = LinkedHashMap<String, String>()
                    var j = 0
                    for (i in 0..mForm!!.size() - 1) {
                        val target = mForm!!.getItemByIndex(i)
                        val tagName = target!!.tagName()
                        if ("select".equals(tagName, ignoreCase = true)) {
                            val spinner = mBaseLinearLayout!!.getChildAt(j++) as Spinner
                            val elements = target.select("option")
                            val idx = spinner.selectedItemPosition
                            if (idx != 0) {
                                selectionChanged = true
                            }
                            map.put(target.attr("name"), elements[idx].attr("value"))
                        } else if ("input".equals(tagName, ignoreCase = true)) {
                            if ("text".equals(target.attr("type"), ignoreCase = true)) {
                                val editText = mBaseLinearLayout!!.getChildAt(j++) as MaterialEditText
                                val value = editText.text.toString()
                                if (!TextUtils.isEmpty(value)) {
                                    selectionChanged = true
                                }
                                map.put(target.attr("name"), value)
                            } else if (Pattern.compile(FormUtils.INVISIBLE_FORM_ITEM_PATTERN).matcher(target.toString()).find()) {
                                map.put(target.attr("name"), target.attr("value"))
                            }
                        } else {
                            map.put(target.attr("name"), target.attr("value"))
                        }
                    }
                    mPresenter!!.loadQuery(fragmentManager, mForm!!.action, map, selectionChanged)
                }
                .setNegativeButton(android.R.string.cancel, null)

        return ActivityUtils.addViewToAlertDialog(builder, view)
    }

    companion object {

        private var document: Document? = null
        private var mPresenter: LoginPresenter? = null

        fun newInstance(dom: Document, presenter: LoginPresenter): FormDialog {
            document = dom
            mPresenter = presenter
            return FormDialog()
        }
    }
}
