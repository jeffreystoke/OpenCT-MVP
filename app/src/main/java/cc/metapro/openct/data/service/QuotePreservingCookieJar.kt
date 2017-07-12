package cc.metapro.openct.data.service

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

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.internal.Util.delimiterOffset
import okhttp3.internal.Util.trimSubstring
import okhttp3.internal.platform.Platform
import okhttp3.internal.platform.Platform.WARN
import java.io.IOException
import java.net.CookieHandler
import java.net.HttpCookie
import java.util.*


/**
 * 代码源自 OkHttp项目 QuotePreservingCookieJar.java
 * 开源协议 - Apache License, Version 2.0
 */

/**
 * A cookie jar that delegates to a [java.net.CookieHandler].
 */
class QuotePreservingCookieJar(private val cookieHandler: CookieHandler?) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookieHandler != null) {
            val cookieStrings = ArrayList<String>()
            for (cookie in cookies) {
                cookieStrings.add(cookie.toString().replace("; domain=".toRegex(), "; domain=."))
            }
            val multimap = Collections.singletonMap<String, List<String>>("Set-Cookie", cookieStrings)
            try {
                cookieHandler.put(url.uri(), multimap)
            } catch (e: IOException) {
                Platform.get().log(WARN, "Saving cookies failed for " + url.resolve("/...")!!, e)
            }

        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // The RI passes all headers. We don't have 'em, so we don't pass 'em!
        val headers = emptyMap<String, List<String>>()
        val cookieHeaders: Map<String, List<String>>
        try {
            cookieHeaders = cookieHandler!!.get(url.uri(), headers)
        } catch (e: IOException) {
            //            Platform.get().log(WARN, "Loading cookies failed for " + url.resolve("/..."), e);
            return emptyList()
        }

        var cookies: MutableList<Cookie>? = null
        for ((key, value) in cookieHeaders) {
            if (("Cookie".equals(key, ignoreCase = true) || "Cookie2".equals(key, ignoreCase = true)) && !value.isEmpty()) {
                for (header in value) {
                    if (cookies == null) cookies = ArrayList<Cookie>()
                    cookies.addAll(decodeHeaderAsJavaNetCookies(url, header))
                }
            }
        }

        return if (cookies != null)
            Collections.unmodifiableList(cookies)
        else
            emptyList<Cookie>()
    }

    /**
     * Convert a request header to OkHttp's cookies via [HttpCookie]. That extra step handles
     * multiple cookies in a single request header, which [Cookie.parse] doesn't support.
     */
    private fun decodeHeaderAsJavaNetCookies(url: HttpUrl, header: String): List<Cookie> {
        val result = ArrayList<Cookie>()
        var pos = 0
        val limit = header.length
        var pairEnd: Int
        while (pos < limit) {
            pairEnd = delimiterOffset(header, pos, limit, ";,")
            val equalsSign = delimiterOffset(header, pos, pairEnd, '=')
            val name = trimSubstring(header, pos, equalsSign)
            if (name.startsWith("$")) {
                pos = pairEnd + 1
                continue
            }

            // We have either name=value or just a name.
            val value = if (equalsSign < pairEnd)
                trimSubstring(header, equalsSign + 1, pairEnd)
            else
                ""

            result.add(Cookie.Builder()
                    .name(name)
                    .value(value)
                    .domain(url.host())
                    .build())
            pos = pairEnd + 1
        }
        return result
    }
}