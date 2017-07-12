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

import cc.metapro.openct.utils.Constants
import okhttp3.HttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

class CmsFactory(info: UniversityInfo) : UniversityFactory(info, Constants.TYPE_CMS) {

    @Throws(Exception::class)
    fun getPageDom(url: String): Document? {
        INFO_PAGE_URL = HttpUrl.parse(url)!!.toString()
        val response = UniversityFactory.mService!!.get(url).execute()
        val tablePage = response.body()
        return Jsoup.parse(tablePage, url)
    }

    @Throws(Exception::class)
    fun queryGradePageDom(actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean): Document {
        var actionURL = actionURL
        if (Constants.QZDATASOFT.equals(UniversityFactory.SYS, ignoreCase = true)) {
            actionURL = HttpUrl.parse(actionURL)!!.newBuilder().encodedPath("/jsxsd/kscj/cjcx_list").build().url().toString()
        }
        return getFinalPageDom(actionURL, queryMap, needNewPage)
    }

    @Throws(Exception::class)
    fun queryClassPageDom(actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean): Document {
        return getFinalPageDom(actionURL, queryMap, needNewPage)
    }

    @Throws(Exception::class)
    private fun getFinalPageDom(actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean): Document {
        val tablePage: String
        if (Constants.QZDATASOFT.equals(UniversityFactory.SYS, ignoreCase = true)) {
            tablePage = UniversityFactory.mService!!.post(actionURL, queryMap).execute().body()!!
        } else {
            if (needNewPage) {
                tablePage = UniversityFactory.mService!!.post(actionURL, queryMap).execute().body()!!
            } else {
                tablePage = UniversityFactory.mService!!.get(actionURL).execute().body()!!
            }
        }
        return Jsoup.parse(tablePage, INFO_PAGE_URL)
    }

    companion object {

        private var INFO_PAGE_URL: String? = null
    }
}
