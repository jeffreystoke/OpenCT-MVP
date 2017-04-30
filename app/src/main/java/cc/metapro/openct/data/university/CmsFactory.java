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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

import cc.metapro.openct.utils.Constants;
import okhttp3.HttpUrl;
import retrofit2.Response;

public class CmsFactory extends UniversityFactory {

    private static String INFO_PAGE_URL;

    public CmsFactory(UniversityInfo info) {
        super(info, Constants.TYPE_CMS);
    }

    @Nullable
    public Document getPageDom(String url) throws Exception {
        INFO_PAGE_URL = HttpUrl.parse(url).toString();
        Response<String> response = mService.get(url).execute();
        String tablePage = response.body();
        return Jsoup.parse(tablePage, url);
    }

    @NonNull
    public Document queryGradePageDom(String actionURL, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            actionURL = HttpUrl.parse(actionURL).newBuilder().encodedPath("/jsxsd/kscj/cjcx_list").build().url().toString();
        }
        return getFinalPageDom(actionURL, queryMap, needNewPage);
    }

    @NonNull
    public Document queryClassPageDom(String actionURL, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        return getFinalPageDom(actionURL, queryMap, needNewPage);
    }

    @NonNull
    private Document getFinalPageDom(String actionURL, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        String tablePage;
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            tablePage = mService.post(actionURL, queryMap).execute().body();
        } else {
            if (needNewPage) {
                tablePage = mService.post(actionURL, queryMap).execute().body();
            } else {
                tablePage = mService.get(actionURL).execute().body();
            }
        }
        return Jsoup.parse(tablePage, INFO_PAGE_URL);
    }
}
