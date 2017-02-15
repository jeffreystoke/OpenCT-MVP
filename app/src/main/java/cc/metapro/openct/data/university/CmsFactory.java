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
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Map;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.utils.Constants;
import okhttp3.HttpUrl;
import retrofit2.Response;

@Keep
public class CmsFactory extends UniversityFactory {

    private static String CLASS_URL;
    private static String GRADE_URL;

    public CmsFactory(String cmsSys, String baseURL) {
        super(cmsSys, baseURL);
    }

    @Nullable
    public Document getClassPageDom(String url) throws Exception {
        CLASS_URL = HttpUrl.parse(url).toString();
        Response<String> response = mService.getPage(url, webHelper.getUserCenterURL()).execute();
        String tablePage = response.body();
        return Jsoup.parse(tablePage, url);
    }

    @Nullable
    public Document getGradePageDom(String url) throws Exception {
        GRADE_URL = HttpUrl.parse(url).toString();
        Response<String> response = mService.getPage(url, webHelper.getUserCenterURL()).execute();
        String tablePage = response.body();
        return Jsoup.parse(tablePage, url);
    }

    @NonNull
    public Document getFinalGradePageDom(String actionURL, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            actionURL = HttpUrl.parse(actionURL).newBuilder().encodedPath("/jsxsd/kscj/cjcx_list").build().url().toString();
        }
        return getFinalPageDom(actionURL, GRADE_URL, queryMap, needNewPage);
    }

    @NonNull
    public Document getFinalClassPageDom(String actionURL, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        return getFinalPageDom(actionURL, CLASS_URL, queryMap, needNewPage);
    }

    @NonNull
    private Document getFinalPageDom(String actionURL, String refer, Map<String, String> queryMap, boolean needNewPage) throws Exception {
        String tablePage;
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            tablePage = mService.post(actionURL, refer, queryMap).execute().body();
        } else {
            if (needNewPage) {
                tablePage = mService.post(actionURL, refer, queryMap).execute().body();
            } else {
                tablePage = mService.getPage(actionURL, refer).execute().body();
            }
        }
        tablePage = tablePage.replaceAll(HTMLUtils.BR, HTMLUtils.BR_REPLACER);

        return Jsoup.parse(tablePage, refer);
    }

    public static class ClassTableInfo {

        public int mNameIndex, mTypeIndex, mDuringIndex, mPlaceIndex, mTimeIndex, mTeacherIndex;

        public String mClassTableID;

        // Regular Expressions for class info parse
        public String mNameRE, mTypeRE, mDuringRE, mTimeRE, mTeacherRE, mPlaceRE;
    }

}
