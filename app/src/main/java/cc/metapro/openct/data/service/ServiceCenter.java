package cc.metapro.openct.data.service;

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

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import cc.metapro.openct.grades.cet.CETService;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServiceCenter {

    public static CETService createCETService() {
        return new Retrofit.Builder()
                .baseUrl("http://www.chsi.com.cn/cet/")
                .client(new OkHttpClient.Builder()
                        .cookieJar(new QuotePreservingCookieJar(new CookieManager() {{
                            setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                        }}))
                        .followRedirects(true)
                        .connectTimeout(20, TimeUnit.SECONDS)
                        .build())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build().create(CETService.class);
    }

    public static GitRepoService createOpenCTService() {
        return new Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/jeffreystoke/openct-school-info/master/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(GitRepoService.class);
    }
}
