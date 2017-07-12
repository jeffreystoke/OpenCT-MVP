package cc.metapro.openct.utils

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

import android.app.ProgressDialog
import android.content.Context
import android.support.annotation.StringRes
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ScrollView
import cc.metapro.openct.R
import cc.metapro.openct.custom.CustomActivity
import cc.metapro.openct.customviews.CaptchaDialog
import cc.metapro.openct.customviews.LinkSelectionDialog
import cc.metapro.openct.customviews.TableChooseDialog
import cc.metapro.openct.utils.base.LoginPresenter
import org.jsoup.nodes.Document

object ActivityUtils {

    private var sProgressDialog: ProgressDialog? = null

    fun showCaptchaDialog(manager: FragmentManager, presenter: LoginPresenter) {
        try {
            CaptchaDialog.newInstance(presenter).show(manager, "captcha_dialog")
        } catch (ignored: Exception) {

        }

    }

    fun showLinkSelectionDialog(manager: FragmentManager, type: String, document: Document, presenter: LoginPresenter) {
        LinkSelectionDialog.newInstance(type, document, presenter, true, false).show(manager, "link_selection_dialog")
    }

    fun showTableChooseDialog(manager: FragmentManager, type: String, document: Document, presenter: LoginPresenter?) {
        TableChooseDialog.newInstance(type, document, presenter).show(manager, "table_choose_dialog")
    }

    fun showAdvCustomTip(context: Context, type: String) {
        AlertDialog.Builder(context)
                .setTitle(R.string.load_fail)
                .setMessage(R.string.load_fail_tip)
                .setPositiveButton(android.R.string.ok) { dialog, which -> CustomActivity.actionStart(context, type) }
                .setNegativeButton(android.R.string.cancel) { dialog, which -> }
                .create().show()
    }

    private fun showProgressDialog(context: Context, @StringRes messageId: Int, cancelable: Boolean) {
        if (sProgressDialog != null) {
            sProgressDialog!!.dismiss()
            sProgressDialog = null
        }
        sProgressDialog = ProgressDialog(context)
        sProgressDialog!!.setMessage(context.getString(messageId))
        sProgressDialog!!.setCancelable(cancelable)
        sProgressDialog!!.show()
    }

    fun showProgressDialog(context: Context, @StringRes messageId: Int) {
        showProgressDialog(context, messageId, true)
    }

    fun dismissProgressDialog() {
        if (sProgressDialog != null) {
            sProgressDialog!!.dismiss()
            sProgressDialog = null
        }
    }

    fun addViewToAlertDialog(builder: AlertDialog.Builder, view: View): AlertDialog {
        val parent = view.parent as ViewGroup
        parent?.removeView(view)

        val scrollView = ScrollView(builder.context)
        scrollView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        scrollView.addView(view)

        builder.setOnDismissListener {
            val imm = builder.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        val dialog = builder.setView(scrollView).create()
        val window = dialog.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return dialog
    }

    fun getAlertBuilder(context: Context, @StringRes title: Int): AlertDialog.Builder {
        return AlertDialog.Builder(context)
                .setTitle(title)
                .setPositiveButton(android.R.string.ok, null)
    }
}
