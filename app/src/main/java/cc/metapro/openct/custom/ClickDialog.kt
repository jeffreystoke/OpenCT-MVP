package cc.metapro.openct.custom

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

import cc.metapro.interactiveweb.InteractiveWebView
import cc.metapro.openct.R
import cc.metapro.openct.utils.base.BaseDialog

class ClickDialog : BaseDialog() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(R.string.question)
                .setMessage(R.string.what_did_you_clicked)
                .setPositiveButton(R.string.common_input) { _, _ ->
                    mTypeCallback!!.onResult(InteractiveWebView.COMMON_INPUT_FLAG)
                    dismiss()
                }
                .setNeutralButton(R.string.username_input) { _, _ ->
                    mTypeCallback!!.onResult(InteractiveWebView.USERNAME_INPUT_FLAG)
                    dismiss()
                }
                .create()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTypeCallback = null
    }

    interface TypeCallback {
        fun onResult(type: String)
    }

    companion object {
        private var mTypeCallback: TypeCallback? = null

        fun newInstance(typeCallback: (Any) -> Unit): ClickDialog {
            mTypeCallback = typeCallback
            return ClickDialog()
        }
    }
}
