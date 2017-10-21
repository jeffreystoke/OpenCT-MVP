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

package cc.metapro.openct.data.service

import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.http.*

interface UniversityService {

    /**
     * Http Post 操作
     * @param url Post 地址
     * @param postMap Post 表单
     */
    @POST
    @FormUrlEncoded
    fun post(@Url url: String, @FieldMap postMap: Map<String, String>): Observable<String>

    /**
     * 根据链接获取图片
     * @param url 图片地址
     */
    @Streaming
    @GET
    fun getPic(@Url url: String): Observable<ResponseBody>

    /**
     * Http Get 含有 UrlParam
     * @param url Get 地址
     * @param paramMap UrlParam 键值对
     */
    @GET
    fun paramGet(@Url url: String, @QueryMap paramMap: Map<String, String>): Observable<String>

    /**
     * Http Get 不含有任何参数
     * @param url Get 地址
     */
    @GET
    operator fun get(@Url url: String): Observable<String>
}