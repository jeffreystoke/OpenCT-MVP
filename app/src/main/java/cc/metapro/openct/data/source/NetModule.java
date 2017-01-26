package cc.metapro.openct.data.source;

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

import com.google.gson.Gson;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import cc.metapro.openct.data.openctservice.QuotePreservingCookieJar;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Module
public class NetModule {

    @Provides
    @Singleton
    OkHttpClient provideSchoolOkHttpClient() {
        return new OkHttpClient.Builder()
                .cookieJar(new QuotePreservingCookieJar(
                        new CookieManager() {{
                            setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                        }}))
                .followRedirects(true)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    Retrofit provideHtmlRetrofit(OkHttpClient client, String baseURL) {
        return new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    Retrofit provideJsonRetrofit(OkHttpClient client) {
        return new Retrofit.Builder()
                .baseUrl("http://openct.metapro.cc/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
