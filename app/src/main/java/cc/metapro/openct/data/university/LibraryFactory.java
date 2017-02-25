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
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.data.university.item.BookInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormHandler;
import cc.metapro.openct.utils.webutils.FormUtils;
import okhttp3.HttpUrl;
import retrofit2.Call;

@Keep
public class LibraryFactory extends UniversityFactory {

    private static final String TAG = LibraryFactory.class.getSimpleName();
    private static final Pattern nextPagePattern = Pattern.compile("(下一?1?页)");
    private static String nextPageURL = "";
    private LibURLFactory mURLFactory;

    public LibraryFactory(String libSys, String libBaseURL) {
        super(libSys, libBaseURL);
        mURLFactory = new LibURLFactory(libBaseURL);
    }

    @NonNull
    public Document getBorrowPageDom(String url) throws Exception {
        String tablePage = mService.getPage(url).execute().body();
        tablePage = tablePage.replaceAll(HTMLUtils.BR, HTMLUtils.BR_REPLACER);
        return Jsoup.parse(tablePage, url);
    }

    @NonNull
    public List<BookInfo> search(@NonNull Map<String, String> searchMap) throws Exception {
        checkService();
        nextPageURL = "";
        String searchPage = null;
        try {
            searchPage = mService.getPage(mURLFactory.SEARCH_URL).execute().body();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }

        if (searchPage == null) {
            return new ArrayList<>(0);
        }

        FormHandler handler = new FormHandler(searchPage, mURLFactory.SEARCH_URL);
        Form form = handler.getForm(0);

        if (form == null) {
            return new ArrayList<>(0);
        }
        searchMap.put(Constants.SEARCH_TYPE_KEY, typeTrans(searchMap.get(Constants.SEARCH_CONTENT_KEY)));
        Map<String, String> postMap = FormUtils.getLibSearchQueryMap(form, searchMap);

        String action = postMap.get(Constants.ACTION_KEY);
        postMap.remove(Constants.ACTION_KEY);
        Call<String> call = mService.searchLibrary(action, postMap);
        String resultPage = call.execute().body();
        prepareNextPageURL(resultPage);
        return TextUtils.isEmpty(resultPage) ? new ArrayList<BookInfo>(0) : parseBook(resultPage);
    }

    @NonNull
    public List<BookInfo> getNextPage() throws Exception {
        String resultPage = null;
        if (TextUtils.isEmpty(nextPageURL)) {
            return new ArrayList<>(0);
        }
        if (Constants.NJHUIWEN.equalsIgnoreCase(SYS)) {
            resultPage = mService.getPage(nextPageURL).execute().body();
        }
        prepareNextPageURL(resultPage);
        return TextUtils.isEmpty(resultPage) ? new ArrayList<BookInfo>(0) : parseBook(resultPage);
    }

    private void prepareNextPageURL(String resultPage) {
        Document result = Jsoup.parse(resultPage, mURLFactory.SEARCH_REF);
        Elements links = result.select("a");
        boolean found = false;
        for (Element a : links) {
            if (nextPagePattern.matcher(a.text()).find()) {
                String tmp = a.absUrl("href");
                if (!tmp.equals(nextPageURL)) {
                    nextPageURL = tmp;
                }
                found = true;
                break;
            }
        }
        if (!found) {
            nextPageURL = "";
        }
    }

    @NonNull
    private String typeTrans(String cnType) {
        switch (cnType) {
            case "书名":
                return "title";
            case "作者":
                return "author";
            case "ISBN":
                return "isbn";
            case "出版社":
                return "publisher";
            case "丛书名":
                return "series";
            default:
                return "title";
        }
    }

    @NonNull
    private List<BookInfo> parseBook(@NonNull String resultPage) {
        List<BookInfo> bookList = new ArrayList<>();
        Document document = Jsoup.parse(resultPage, mURLFactory.SEARCH_URL);
        Elements elements = document.select("li[class=book_list_info]");
        Elements tmp = document.select("div[class=list_books]");
        elements.addAll(tmp);
        for (Element entry : elements) {
            Elements els_title = entry.children().select("h3");
            String tmp_1 = els_title.text();
            String title = els_title.select("a").text();
            String href = els_title.select("a").get(0).absUrl("href");

            if (TextUtils.isEmpty(title)) return new ArrayList<>(0);

            title = title.split("\\.")[1];
            String[] tmps = tmp_1.split(" ");
            String content = tmps[tmps.length - 1];
            Elements els_body = entry.children().select("p");
            String author = els_body.text();
            els_body = els_body.select("span");
            String remains = els_body.text();
            author = author.substring(author.indexOf(remains) + remains.length());
            BookInfo b = new BookInfo(title, author, content, remains, href);
            bookList.add(b);
        }
        return bookList;
    }

    @Override
    protected String getBaseURL() {
        return mURLFactory.LOGIN_REF;
    }

    private class LibURLFactory {

        String LOGIN_URL, LOGIN_REF, SEARCH_URL, SEARCH_REF;

        LibURLFactory(String libBaseURL) {
            libBaseURL = HttpUrl.parse(libBaseURL).toString();

            if (Constants.NJHUIWEN.equalsIgnoreCase(SYS)) {
                SEARCH_URL = libBaseURL + "opac/search.php";
                SEARCH_REF = libBaseURL + "opac/openlink.php";
                LOGIN_URL = libBaseURL + "reader/redr_verify.php";
                LOGIN_REF = libBaseURL + "reader/login.php";
            }
        }
    }
}
