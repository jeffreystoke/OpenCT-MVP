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
import android.util.Log
import cc.metapro.openct.data.university.model.BookInfo
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.webutils.FormHandler
import cc.metapro.openct.utils.webutils.FormUtils
import okhttp3.HttpUrl
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.util.*
import java.util.regex.Pattern

class LibraryFactory(info: UniversityInfo) : UniversityFactory(info, Constants.TYPE_LIB) {
    private val mURLFactory: LibURLFactory

    init {
        mURLFactory = LibURLFactory(info.libURL)
    }

    @Throws(Exception::class)
    fun getBorrowPageDom(url: String): Document {
        var tablePage: String = UniversityFactory.mService!!.get(url).execute().body()!!
//        tablePage = tablePage.replace(HTMLUtils.BR.toRegex(), HTMLUtils.BR_REPLACER)
        return Jsoup.parse(tablePage, url)
    }

    @Throws(Exception::class)
    fun search(searchMap: MutableMap<String, String>): List<BookInfo> {
        checkService()
        nextPageURL = ""
        var searchPage: String? = null
        try {
            searchPage = UniversityFactory.mService!!.get(mURLFactory.SEARCH_URL).execute().body()
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
        }

        if (searchPage == null) {
            return ArrayList(0)
        }

        val handler = FormHandler(searchPage, mURLFactory.SEARCH_URL)
        val form = handler.getForm(0) ?: return ArrayList(0)

        searchMap.put(Constants.SEARCH_TYPE_KEY, typeTrans(searchMap[Constants.SEARCH_CONTENT_KEY]!!))
        val postMap = FormUtils.getLibSearchQueryMap(form, searchMap).toMutableMap()

        val action = postMap[Constants.ACTION_KEY]
        postMap.remove(Constants.ACTION_KEY)
        val call = UniversityFactory.mService!!.paramGet(action!!, postMap)
        val resultPage = call.execute().body()
        prepareNextPageURL(resultPage!!)
        return if (TextUtils.isEmpty(resultPage)) ArrayList<BookInfo>(0) else parseBook(resultPage!!)
    }

    val nextPage: List<BookInfo>
        @Throws(Exception::class)
        get() {
            var resultPage: String? = null
            if (TextUtils.isEmpty(nextPageURL)) {
                return ArrayList(0)
            }
            if (Constants.NJHUIWEN.equals(UniversityFactory.SYS, ignoreCase = true)) {
                resultPage = UniversityFactory.mService!!.get(nextPageURL).execute().body()
            }
            prepareNextPageURL(resultPage!!)
            return if (TextUtils.isEmpty(resultPage)) ArrayList<BookInfo>(0) else parseBook(resultPage!!)
        }

    private fun prepareNextPageURL(resultPage: String) {
        val result = Jsoup.parse(resultPage, mURLFactory.SEARCH_REF)
        val links = result.select("a")
        var found = false
        for (a in links) {
            if (nextPagePattern.matcher(a.text()).find()) {
                val tmp = a.absUrl("href")
                if (tmp != nextPageURL) {
                    nextPageURL = tmp
                }
                found = true
                break
            }
        }
        if (!found) {
            nextPageURL = ""
        }
    }

    private fun typeTrans(cnType: String): String {
        when (cnType) {
            "书名", "Title" -> return "title"
            "作者", "Author" -> return "author"
            "ISBN" -> return "isbn"
            "出版社", "Press" -> return "publisher"
            else -> return "title"
        }
    }

    private fun parseBook(resultPage: String): List<BookInfo> {
        val bookList = ArrayList<BookInfo>()
        val document = Jsoup.parse(resultPage, mURLFactory.SEARCH_URL)
        val elements = document.select("li[class=book_list_info]")
        val tmp = document.select("div[class=list_books]")
        elements.addAll(tmp)
        for (entry in elements) {
            val els_title = entry.children().select("h3")
            val tmp_1 = els_title.text()
            var title = els_title.select("a").text()
            val href = els_title.select("a")[0].absUrl("href")

            if (TextUtils.isEmpty(title)) return ArrayList(0)

            title = title.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            val tmps = tmp_1.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val content = tmps[tmps.size - 1]
            var els_body = entry.children().select("p")
            var author = els_body.text()
            els_body = els_body.select("span")
            val remains = els_body.text()
            author = author.substring(author.indexOf(remains) + remains.length)
            val b = BookInfo(title, author, content, remains, href)
            bookList.add(b)
        }
        return bookList
    }

    override var baseURL: String = ""
        get() = mURLFactory.LOGIN_REF

    private inner class LibURLFactory internal constructor(libBaseURL: String) {

        internal var LOGIN_URL: String
        internal var LOGIN_REF: String
        internal var SEARCH_URL: String
        internal var SEARCH_REF: String

        init {
            val baseUrl = HttpUrl.parse(libBaseURL)

            if (Constants.NJHUIWEN.equals(UniversityFactory.SYS, ignoreCase = true)) {
                SEARCH_URL = baseUrl!!.newBuilder("opac/search.php")!!.toString()
                SEARCH_REF = baseUrl.newBuilder("opac/openlink.php")!!.toString()
                LOGIN_URL = baseUrl.newBuilder("reader/redr_verify.php")!!.toString()
                LOGIN_REF = baseUrl.newBuilder("reader/login.php")!!.toString()
            } else {
                SEARCH_URL = libBaseURL
                SEARCH_REF = libBaseURL
                LOGIN_URL = libBaseURL
                LOGIN_REF = libBaseURL
            }
        }
    }

    companion object {

        private val TAG = LibraryFactory::class.java.simpleName
        private val nextPagePattern = Pattern.compile("(下一?1?页)")
        private var nextPageURL = ""
    }
}
