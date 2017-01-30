package cc.metapro.openct.data.university;

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

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

@Keep
public interface UniversityService {

    @POST
    @FormUrlEncoded
    Call<String> login(
            @Url String url,
            @Header("Referer") String referer,
            @FieldMap Map<String, String> loginMap
    );

    @POST
    @FormUrlEncoded
    Call<String> post(
            @Url String url,
            @Header("Referer") String referer,
            @FieldMap Map<String, String> postMap
    );

    @Streaming
    @GET
    Call<ResponseBody> getCAPTCHA(
            @Url String url
    );

    @GET
    Call<String> searchLibrary(
            @Url String url,
            @Header("Referer") String referer,
            @QueryMap Map<String, String> searchMap
    );

    @GET
    Call<String> getPage(
            @Url String url,
            @Header("Referer") String referer
    );
}
