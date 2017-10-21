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
import cc.metapro.openct.data.QuotePreservingCookieJar
import cc.metapro.openct.data.service.UniversityService
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.interceptors.SchoolInterceptor
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.IOException
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.*
import java.util.concurrent.TimeUnit

internal class WebHelper(baseURL: String) {

    val baseURL: HttpUrl
    private var mCaptchaUrl: HttpUrl? = null
    var loginPageURL: HttpUrl? = null
        private set
    private var mUserCenterUrl: HttpUrl? = null

    var loginForm: Element? = null
        private set
    var loginPageDOM: Document? = null
        private set
    val interceptor: SchoolInterceptor

    init {
        this.baseURL = HttpUrl.parse(URLUtil.guessUrl(baseURL))!!
        loginPageURL = this.baseURL
        mUserCenterUrl = this.baseURL

        interceptor = SchoolInterceptor(this.baseURL)
    }

    fun createSchoolService(): UniversityService {
        return Retrofit.Builder()
                .baseUrl(URLUtil.guessUrl(baseURL.host()))
                .client(OkHttpClient.Builder()
                        .addNetworkInterceptor(interceptor)
                        .followRedirects(true)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .cookieJar(QuotePreservingCookieJar(object : CookieManager() {
                            init {
                                setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                            }
                        })).build()
                )
                .addConverterFactory(ScalarsConverterFactory.create())
                .build()
                .create(UniversityService::class.java)
    }

    val captchaURL: String?
        get() = if (mCaptchaUrl == null) null else mCaptchaUrl!!.toString()

    @Throws(IOException::class)
    fun parseCaptchaURL(sys: String, loginPage: String, service: UniversityService) {
        val document = Jsoup.parse(loginPage, loginPageURL!!.toString())

        val domList = ArrayList<Document>()
        domList.add(document)
        val iFrames = document.select("iframe")
        iFrames.addAll(document.select("span:matches(登.*?录)"))
        for (iFrame in iFrames) {
            var url = iFrame.attr("src")
            if (TextUtils.isEmpty(url)) {
                url = iFrame.absUrl("value")
            } else {
                url = iFrame.absUrl("src")
            }
            if (!TextUtils.isEmpty(url)) {
//                val frame = service.get(url).execute().body()
//                domList.add(Jsoup.parse(frame, url))
            }
        }
        setCaptchaURL(domList, sys)
    }

    private fun setCaptchaURL(documents: List<Document>, system: String) {
        loop@ for (document in documents) {
            loginPageDOM = document
            loginPageURL = HttpUrl.parse(document.baseUri())
            val forms = document.select("form")

            // 遍历表单
            for (form in forms) {
                val codeImg = form.select("img[src~=(?i)^(?!.*(png|jpg|gif|ico)).*$")
                codeImg.addAll(form.select("iframe[src~=(?i)^(?!.*(png|jpg|gif|ico)).*$"))
                if (Constants.KINGOSOFT.equals(system, ignoreCase = true)) {
                    val captcha = Element(Tag.valueOf("img"), document.baseUri())
                    captcha.attr("src", "../sys/ValidateCode.aspx")
                    codeImg.add(captcha)
                    form.appendChild(form.parent().after(form))
                }
                // 获取验证码地址
                for (img in codeImg) {
                    val url = img.absUrl("src")
                    if (!TextUtils.isEmpty(url)) {
                        loginForm = form
                        mCaptchaUrl = HttpUrl.parse(URLUtil.guessUrl(url))
                        break@loop
                    }
                }
            }
        }
    }

    fun setLoginPageURL(loginPageURL: String) {
        this.loginPageURL = this.loginPageURL!!.newBuilder(loginPageURL)!!.build()
        // 设置默认用户中心首页为登录页, 防止未跳转的情况
        mUserCenterUrl = this.loginPageURL
    }

    var userCenterURL: String
        get() = mUserCenterUrl!!.toString()
        set(userCenterURL) {
            mUserCenterUrl = mUserCenterUrl!!.newBuilder(userCenterURL)!!.build()
        }
}
