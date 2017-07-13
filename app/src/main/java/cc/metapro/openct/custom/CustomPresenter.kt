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

import android.content.Context
import android.support.v4.app.FragmentManager
import cc.metapro.interactiveweb.InteractiveWebView
import cc.metapro.interactiveweb.utils.HTMLUtils
import cc.metapro.openct.data.LocalUser
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.utils.Constants
import java.security.GeneralSecurityException

internal class CustomPresenter(private val mContext: Context, view: CustomContract.View, private val mType: String) : CustomContract.Presenter {
    private var mUsername: String? = null
    private var mPassword: String? = null

    init {
        view.setPresenter(this)
    }

    override fun subscribe() {
        val user: LocalUser?
        when (mType) {
            Constants.TYPE_CLASS -> user = LocalHelper.getCmsStuInfo(mContext)
            Constants.TYPE_GRADE -> user = LocalHelper.getCmsStuInfo(mContext)
            Constants.TYPE_BORROW -> user = LocalHelper.getLibStuInfo(mContext)
            else -> user = LocalHelper.getCmsStuInfo(mContext)
        }
        mUsername = user.username
        try {
            mPassword = user.password
        } catch (e: GeneralSecurityException) {
            mPassword = ""
        }
    }

    override fun unSubscribe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setWebView(webView: InteractiveWebView, manager: FragmentManager) {
        webView.setUserClickCallback { element ->
            if (HTMLUtils.isPasswordInput(element)) {
                if (!webView.setById(element.id(), "value", mPassword)) {
                    webView.setByName(element.attr("name"), "value", mPassword)
                }
            } else if (HTMLUtils.isTextInput(element)) {
                class s : ClickDialog.TypeCallback {
                    override fun onResult(type: String) {
                        when (type) {
                            InteractiveWebView.COMMON_INPUT_FLAG -> if (!webView.focusById(element.id())) {
                                webView.focusByName(element.attr("name"))
                            }
                            InteractiveWebView.USERNAME_INPUT_FLAG -> if (!webView.setById(element.id(), "value", mUsername)) {
                                webView.setByName(element.attr("name"), "value", mUsername)
                            }
                        }
                    }
                }

                ClickDialog.newInstance(s()).show(manager, "click_dialog")
            }
        }
    }
}
