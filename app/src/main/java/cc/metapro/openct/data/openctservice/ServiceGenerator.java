package cc.metapro.openct.data.openctservice;

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

import android.support.annotation.Keep;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@Keep
public class ServiceGenerator {

    public static final String GSON_CONVERTER = "gson", HTML = "html";

    private static final String API_BASE_URL = "http://openct.metapro.cc/";

    private static CookieManager manager = new CookieManager() {{
        setCookiePolicy(CookiePolicy.ACCEPT_ALL);
    }};

    private static final OkHttpClient client =
            new OkHttpClient.Builder()
                    .cookieJar(new QuotePreservingCookieJar(manager))
                    .followRedirects(true)
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .build();

    public static <S> S createService(Class<S> serviceClass, String convertType) {
        Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL).client(client);

        Retrofit retrofit = null;

        if (GSON_CONVERTER.equals(convertType)) {
            retrofit = builder
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else if (HTML.equals(convertType)) {
            retrofit = builder
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }

        return retrofit == null ? null : retrofit.create(serviceClass);
    }
}
