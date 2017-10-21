package cc.metapro.openct.data.university

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

import android.text.TextUtils
import android.webkit.URLUtil
import cc.metapro.openct.LoginConfig
import cc.metapro.openct.data.service.UniversityService
import cc.metapro.openct.data.source.RemoteSource
import cc.metapro.openct.utils.Constants.ACTION_KEY
import cc.metapro.openct.utils.Constants.CAPTCHA_KEY
import cc.metapro.openct.utils.Constants.PASSWORD_KEY
import cc.metapro.openct.utils.Constants.TYPE_CMS
import cc.metapro.openct.utils.Constants.TYPE_LIB
import cc.metapro.openct.utils.Constants.USERNAME_KEY
import cc.metapro.openct.utils.interceptors.SchoolInterceptor
import cc.metapro.openct.utils.webutils.Form
import cc.metapro.openct.utils.webutils.FormHandler
import cc.metapro.openct.utils.webutils.FormUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.util.regex.Pattern

abstract class UniversityFactory internal constructor(info: UniversityInfo, type: Int) {

    private val mRemoteRepoSource: RemoteSource = RemoteSource(info.name)
    private var mUsername: String? = null
    private var mPassword: String? = null
    private var mCaptcha: String? = null
    private var loginSuccess: Boolean = false

    init {
        if (type == TYPE_CMS) {
            SYS = info.cmsSys
            BASE_URL = URLUtil.guessUrl(info.cmsURL)
        } else if (type == TYPE_LIB) {
            SYS = info.libSys
            BASE_URL = URLUtil.guessUrl(info.libURL)
        }
    }

    @Throws(Exception::class)
    fun login(loginMap: Map<String, String>): Document {
        // 准备监听登录结果, 若成功会 302 跳转用户中心
        loginSuccess = false
        class x : SchoolInterceptor.RedirectObserver<String> {
            override fun onRedirect(x: String) {
                webHelper!!.userCenterURL = x
                loginSuccess = true
            }
        }
        interceptor!!.setObserver(x())

        mUsername = loginMap[USERNAME_KEY]
        mPassword = loginMap[PASSWORD_KEY]
        mCaptcha = loginMap[CAPTCHA_KEY]

        val LOGIN_FAIL = Exception("登录失败, 请检查您的用户名和密码\n" + "(以及验证码)")
        var USER_CENTER = ""

        if (mLoginConfig != null && !mLoginConfig!!.isEmpty) {
            return fineLogin()
        }

        // common login
        val loginPageDom = webHelper!!.loginPageDOM
        if (loginPageDom != null) {
            val formHandler = FormHandler(loginPageDom)
            val targetForm = (if (webHelper!!.loginForm == null) formHandler.getForm(0) else Form(webHelper!!.loginForm!!)) ?: throw Exception("观测到学校服务器出了点问题~")

            val res = FormUtils.getLoginFiledMap(targetForm, loginMap, true).toMutableMap()
            val action = res[ACTION_KEY]
            res.remove(ACTION_KEY)
//            val stringResponse = mService!!.post(action!!, res).execute()
//            USER_CENTER = stringResponse.body()!!

            // beat 5 seconds restriction
            if (USER_CENTER.length < 100) {
                Thread.sleep((6 * 1000).toLong())
//                USER_CENTER = mService!!.post(action, res).execute().body()!!
            }

            val document = Jsoup.parse(USER_CENTER, webHelper!!.userCenterURL)
            val frames = document.select("frame")
            frames.addAll(document.select("iframe"))
            for (frame in frames) {
                val url = frame.absUrl("src")
                if (!TextUtils.isEmpty(url)) {
//                    document.append(mService!!.get(url).execute().body()!!)
                }
            }
            // fineLogin finish, check results
            if (loginSuccess || !TextUtils.isEmpty(USER_CENTER) && LOGIN_SUCCESS_PATTERN.matcher(USER_CENTER).find()) {
                return document
            } else {
                throw LOGIN_FAIL
            }
        }

        return Jsoup.parse(USER_CENTER, webHelper!!.userCenterURL)
    }

    @Throws(IOException::class)
    private fun fineLogin(): Document {

        mLoginConfig!!.setInfo(mUsername, mPassword, mCaptcha)
        // get extra part
        if (mLoginConfig!!.needExtraLoginPart()) {
            val extraPartUrl = webHelper!!.loginPageURL!!
                    .newBuilder(mLoginConfig!!.extraLoginPartURL)!!
                    .toString()
            var extraPart = ""
            val method = if (TextUtils.isEmpty(mLoginConfig!!.fetchExtraMethod))
                "GET"
            else
                mLoginConfig!!.fetchExtraMethod.toUpperCase()
            if (method == "POST") {
//                extraPart = mService!!.post(extraPartUrl, HashMap<String, String>(0)).execute().body()!!
            } else if (method == "GET") {
//                extraPart = mService!!.get(extraPartUrl).execute().body()!!
            }
            mLoginConfig!!.setExtraPart(extraPart)
        }

        // get form action url
        val action: String
        if (!TextUtils.isEmpty(mLoginConfig!!.postURL)) {
            action = mLoginConfig!!.postURL
        } else {
            action = UniversityUtils.getLoginFormAction(webHelper!!)
        }

        val keyValueSpec = mLoginConfig!!.postKeyValueSpec
//        val userCenter = mService!!.post(action, keyValueSpec).execute().body()
//        return Jsoup.parse(userCenter, webHelper!!.userCenterURL)
        return Jsoup.parse("")
    }

    // 初次获取验证码
    @Throws(IOException::class)
    fun prepareOnlineInfo(): Boolean {
        destroyService()
        checkService()

        // 获取登录页面后, 若是动态地址则发生302重定向, 更新 LoginRefer 以及 LoginPage 地址 (默认是登录地址)
        class x : SchoolInterceptor.RedirectObserver<String> {
            override fun onRedirect(x: String) {
                webHelper!!.setLoginPageURL(x)
            }
        }
        interceptor!!.setObserver(x())

        // 获取精确配置
//        mLoginConfig = mRemoteRepoSource.loginConfig
        var loginUrl = ""
        if (mLoginConfig != null) {
//            loginUrl = TextHelper.getFirstWhenNotEmpty(mLoginConfig!!.loginURL, webHelper!!.loginPageURL.toString())
        }

//        val loginPageHtml = mService!!.get(loginUrl).execute().body()
//        webHelper!!.parseCaptchaURL(SYS, loginPageHtml!!, mService!!)

        // 获取验证码图片, 精确配置的优先级高于自动解析
        var captchaURL: String = webHelper!!.captchaURL!!
        if (mLoginConfig != null && !mLoginConfig!!.isEmpty) {
            if (mLoginConfig!!.needCaptcha()) {
                captchaURL = webHelper!!.loginPageURL!!
                        .newBuilder(mLoginConfig!!.captchaURL)!!.toString()
//                val body = mService!!.getPic(captchaURL).execute().body()
//                StoreHelper.storeBytes(CAPTCHA_FILE!!, body!!.byteStream())
            }
            return true
        } else if (!TextUtils.isEmpty(captchaURL)) {
//            val body = mService!!.getPic(captchaURL).execute().body()
//            StoreHelper.storeBytes(CAPTCHA_FILE!!, body!!.byteStream())
            return true
        }

        return false
    }

    internal fun checkService() {
        if (webHelper == null) {
            webHelper = WebHelper(baseURL)
            interceptor = webHelper!!.interceptor
            mService = webHelper!!.createSchoolService()
        }
    }

    private fun destroyService() {
        interceptor = null
        mService = null
        webHelper = null
        mLoginConfig = null
    }

    protected open var baseURL: String = ""
        get() = BASE_URL!!

    companion object {

        private val LOGIN_SUCCESS_PATTERN = Pattern.compile("(当前)|(个人)|(退出)|(注销)")
        internal var mService: UniversityService? = null
        internal lateinit var SYS: String

        private var mLoginConfig: LoginConfig? = null
        private var webHelper: WebHelper? = null
        private var interceptor: SchoolInterceptor? = null
        private var BASE_URL: String? = null

        // 再次获取验证码
        @Throws(IOException::class)
        fun getOneMoreCAPTCHA() {
//            val bodyResponse = mService!!.getPic(webHelper!!.captchaURL!!).execute()
//            val body = bodyResponse.body()
//            StoreHelper.storeBytes(CAPTCHA_FILE!!, body!!.byteStream())
        }
    }

}
