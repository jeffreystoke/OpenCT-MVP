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
import org.jsoup.nodes.Element;

import java.util.Map;

import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import okhttp3.HttpUrl;

@Keep
public class CmsFactory extends UniversityFactory {

    private static String CLASS_URL;
    private static String GRADE_URL;

    public CmsFactory(String cmsSys, String baseURL) {
        super(cmsSys, baseURL);
    }

    @Nullable
    public Form getClassPageFrom(String url) throws Exception {
        CLASS_URL = HttpUrl.parse(url).toString();
        String tablePage = mService.getPage(url, webHelper.getUserCenterURL()).execute().body();
        FormHandler handler = new FormHandler(tablePage, url);
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            return handler.getForm(1);
        }
        return handler.getForm(0);
    }

    @Nullable
    public Form getGradePageForm(String url) throws Exception {
        GRADE_URL = HttpUrl.parse(url).toString();
        String tablePage = mService.getPage(url, webHelper.getUserCenterURL()).execute().body();
        FormHandler handler = new FormHandler(tablePage, url);
        return handler.getForm(0);
    }

    @NonNull
    public Map<String, Element> getGradePageTables(String actionURL, Map<String, String> queryMap) throws Exception {
        // 获取成绩表页面的所有表格 id -> Table
        return getTables(actionURL, GRADE_URL, queryMap);
    }

    @NonNull
    public Map<String, Element> getClassPageTables(String actionURL, Map<String, String> queryMap) throws Exception {
        // 获取课程表页面的所有表格 id -> Table
        return getTables(actionURL, CLASS_URL, queryMap);
    }

    @NonNull
    private Map<String, Element> getTables(String actionURL, String refer, Map<String, String> queryMap) throws Exception {
        String tablePage;
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            tablePage = mService.post(refer, refer, queryMap).execute().body();
        } else {
            tablePage = mService.getPage(actionURL, refer).execute().body();
        }
        tablePage = tablePage.replaceAll(Constants.BR, Constants.BR_REPLACER);

        return getTablesFromTargetPage(Jsoup.parse(tablePage, refer));
    }

    public static class ClassTableInfo {

        public int mNameIndex, mTypeIndex, mDuringIndex,
                mPlaceIndex, mTimeIndex, mTeacherIndex, mClassStringCount;

        public String mClassTableID;

        // Regular Expressions to parse class
        public String mNameRE, mTypeRE, mDuringRE, mTimeRE, mTeacherRE, mPlaceRE;
    }

}
