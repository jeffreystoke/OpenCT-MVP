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

import android.annotation.SuppressLint
import android.content.Context
import android.support.annotation.StringRes
import android.support.design.widget.TextInputEditText
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ScrollView
import cc.metapro.openct.R
import cc.metapro.openct.customviews.LinkSelectionDialog
import cc.metapro.openct.customviews.TableChooseDialog
import cc.metapro.openct.utils.base.LoginPresenter
import com.afollestad.materialdialogs.MaterialDialog
import org.jsoup.nodes.Document

object ActivityUtils {

    @SuppressLint("StaticFieldLeak")
    private var sProgressDialog: MaterialDialog? = null

    fun showCaptchaDialog(context: Context, presenter: LoginPresenter) {
        try {
            val view = LayoutInflater.from(context).inflate(R.layout.dialog_captcha, null, false)
            val img = view.findViewById<ImageView>(R.id.captcha_image)
            val text = view.findViewById<TextInputEditText>(R.id.captcha_text)
            MaterialDialog.Builder(context)
                    .title(R.string.enter_captcha)
                    .customView(view, true)
                    .positiveText(android.R.string.ok)
                    .onPositive { _, _ -> }
                    .negativeText(android.R.string.cancel)
                    .show()
        } catch (ignored: Exception) {
        }

    }

    fun showLinkSelectionDialog(manager: FragmentManager, type: String, document: Document, presenter: LoginPresenter) {
        LinkSelectionDialog.newInstance(type, document, presenter, true, false).show(manager, "link_selection_dialog")
    }

    fun showTableChooseDialog(manager: FragmentManager, type: String, document: Document, presenter: LoginPresenter?) {
        TableChooseDialog.newInstance(type, document, presenter).show(manager, "table_choose_dialog")
    }

    /**
     * 显示通过网页加载对话框
     * @param context
     * @param type 是图书馆还是教务网
     */
    fun showAdvCustomTip(context: Context, type: ActionType) {
        MaterialDialog.Builder(context)
                .title(R.string.load_fail)
                .content(R.string.load_fail_tip)
                .positiveText(android.R.string.ok)
                .negativeText(android.R.string.cancel)
                .onPositive { _, _ ->
                    // CustomActivity.actionStart(context, type)
                }
                .show()
    }

    /**
     * 显示可取消进度对话框, 标题固定, 可设定消息内容
     * @param context
     * @param messageId 消息内容
     */
    fun showProgressDialog(context: Context, @StringRes messageId: Int) {
        showProgressDialog(context, messageId, true)
    }

    /**
     * 显示进度对话框, 标题固定, 可设定消息内容, 是否可取消
     * @param context
     * @param messageId 消息内容
     * @param cancelable 是否可取消
     */
    private fun showProgressDialog(context: Context, @StringRes messageId: Int, cancelable: Boolean) {
        sProgressDialog?.dismiss()
        sProgressDialog = MaterialDialog.Builder(context)
                .title(R.string.dialog_title_please_wait)
                .content(messageId)
                .cancelable(cancelable)
                .build()
        sProgressDialog?.show()
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
