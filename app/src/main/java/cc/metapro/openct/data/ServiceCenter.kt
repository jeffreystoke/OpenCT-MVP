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

package cc.metapro.openct.data

import android.webkit.URLUtil
import cc.metapro.openct.data.service.CETService
import cc.metapro.openct.data.service.GitRepoService
import cc.metapro.openct.data.service.UniversityService
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.CookieManager
import java.net.CookiePolicy
import java.util.concurrent.TimeUnit

object ServiceCenter {

    val cetService: CETService
        get() = Retrofit.Builder()
                .baseUrl("http://www.chsi.com.cn/cet/")
                .client(OkHttpClient.Builder()
                        .cookieJar(QuotePreservingCookieJar(object : CookieManager() {
                            init {
                                setCookiePolicy(CookiePolicy.ACCEPT_ALL)
                            }
                        }))
                        .followRedirects(true)
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(CETService::class.java)

    val gitRepoService: GitRepoService
        get() = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/jeffreystoke/openct-school-info/master/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(GitRepoService::class.java)

    fun createUniversityService(baseUrl: String): UniversityService {
        val url = HttpUrl.parse(baseUrl)
        return Retrofit.Builder()
                .baseUrl(URLUtil.guessUrl(url!!.host()))
                .client(OkHttpClient.Builder()
//                        .addNetworkInterceptor(mInterceptor)
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

}