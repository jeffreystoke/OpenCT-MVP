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
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;

@Keep
public class CmsFactory extends UniversityFactory {

    private CmsURLFactory mURLFactory;

    public CmsFactory(CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;
        mClassTableInfo = cmsInfo.mClassTableInfo;
        mGradeTableInfo = cmsInfo.mGradeTableInfo;

        if (!cmsInfo.mCmsURL.endsWith("/")) {
            cmsInfo.mCmsURL += "/";
        }
        mURLFactory = new CmsURLFactory(cmsInfo.mCmsSys, cmsInfo.mCmsURL);
    }

    private String getTablePage(Map<String, String> loginMap, String pattern, String URL) throws Exception {
        // 登录获取用户中心页面
        String userCenter = login(loginMap);

        String tableURL = null;
        // 第一次, 从用户中心页面解析获取链接
        if (!TextUtils.isEmpty(pattern)) {
            Pattern contentPattern = Pattern.compile(pattern);
            Document doc = Jsoup.parse(userCenter, urlFactory.getUserCenterURL());
            Elements links = doc.select("a");
            for (Element a : links) {
                // 根据界面上的文字提示进行判断
                if (contentPattern.matcher(a.text()).find()) {
                    tableURL = a.absUrl("href");
                    break;
                }
            }
        }

        String tablePage = null;
        // 解析到了链接
        if (!TextUtils.isEmpty(tableURL)) {
            // 解析到表格链接后获取表格页面
            mURLFactory.GRADE_URL = tableURL;
            mURLFactory.CLASS_URL = tableURL;
            tablePage = mService.getPage(tableURL, urlFactory.getUserCenterURL()).execute().body();
        } else {
            // 第二次, 根据 URLFactory 生成的地址获取
            if (!TextUtils.isEmpty(URL)) {
                tablePage = mService.getPage(URL, urlFactory.getUserCenterURL()).execute().body();
            }
        }

        // 课表页面为空
        if (TextUtils.isEmpty(tablePage)) {
            throw new Exception("很抱歉, 没能根据链接获取到页面");
        }

        return tablePage;
    }

    @Nullable
    public Form getClassPageFrom(Map<String, String> loginMap) throws Exception {
        String tablePage = getTablePage(loginMap, mURLFactory.CLASS_URL_PATTERN, mURLFactory.CLASS_URL);
        FormHandler handler = new FormHandler(tablePage, mURLFactory.CLASS_URL);
        if (Constants.QZDATASOFT.equalsIgnoreCase(mCMSInfo.mCmsSys)) {
            return handler.getForm(1);
        }
        return handler.getForm(0);
    }

    @Nullable
    public Form getGradePageForm(Map<String, String> loginMap) throws Exception {
        String tablePage = getTablePage(loginMap, mURLFactory.GRADE_URL_PATTERN, mURLFactory.GRADE_URL);
        FormHandler handler = new FormHandler(tablePage, mURLFactory.GRADE_URL);
        return handler.getForm(0);
    }

    /**
     * tend to get grade info form grade page
     *
     * @return a list of grade info
     * @throws IOException
     * @throws LoginException
     */
    @NonNull
    public List<GradeInfo> getGrades(String actionURL, Map<String, String> queryMap) throws Exception {
        // 获取课程表页面
        String tablePage;
        if (Constants.QZDATASOFT.equalsIgnoreCase(mCMSInfo.mCmsSys)) {
            tablePage = mService.post(mURLFactory.GRADE_URL, mURLFactory.GRADE_URL, queryMap).execute().body();
        } else {
            tablePage = mService.post(actionURL, urlFactory.getUserCenterURL(), queryMap).execute().body();
        }

        destroyService();
        return UniversityUtils.generateGrades(tablePage, mGradeTableInfo);
    }

    public List<EnrichedClassInfo> getClasses(String actionURL, Map<String, String> queryMap) throws Exception {
        String tablePage;
        if (Constants.QZDATASOFT.equalsIgnoreCase(mCMSInfo.mCmsSys)) {
            tablePage = mService.post(mURLFactory.CLASS_URL, mURLFactory.CLASS_URL, queryMap).execute().body();
        } else {
            tablePage = mService.getPage(actionURL, urlFactory.getUserCenterURL()).execute().body();
        }

        // 替换标识符, 生成课程
        tablePage = tablePage.replaceAll(Constants.BR, Constants.BR_REPLACER);
        Document doc = Jsoup.parse(tablePage);

        // 第一次匹配, 根据标准Id 获取 表格
        Elements tables = doc.select("table[id=" + mClassTableInfo.mClassTableID + "]");
        Element targetTable = tables.first();

        // 第一次匹配失败, 第二次匹配, 使用关键字匹配
        if (targetTable == null) {
            tables = doc.select("table:matches((星期)|(周)|(课程))");
            targetTable = tables.first();
        }

        // 两次匹配失败, 返回
        if (targetTable == null) {
            return new ArrayList<>(0);
        }

        // 从表格中获取课程信息
        List<Element> classes = UniversityUtils.getRawClasses(targetTable);

        destroyService();
        return UniversityUtils.generateClasses(classes, mClassTableInfo);
    }

    @Override
    protected String
    getBaseURL() {
        return mCMSInfo.mCmsURL;
    }

    public static class GradeTableInfo {
        public int mClassCodeIndex, mClassNameIndex, mClassTypeIndex,
                mPointsIndex, mGradeSummaryIndex, mGradePracticeIndex,
                mGradeCommonIndex, mGradeMidExamIndex, mGradeFinalExamIndex,
                mGradeMakeupIndex;

        String mGradeTableID;
    }

    public static class ClassTableInfo {

        public int mDailyClasses, mNameIndex, mTypeIndex, mDuringIndex,
                mPlaceIndex, mTimeIndex, mTeacherIndex, mClassStringCount;

        public String mClassTableID;

        // Regular Expressions to parse class
        public String mNameRE, mTypeRE, mDuringRE, mTimeRE, mTeacherRE, mPlaceRE;
    }

    private static class CmsURLFactory {
        String CLASS_URL, GRADE_URL;

        String CLASS_URL_PATTERN, GRADE_URL_PATTERN;

        CmsURLFactory(@NonNull String cmsSys, @NonNull String cmsBaseURL) {
            if (!cmsBaseURL.endsWith("/")) {
                cmsBaseURL += "/";
            }
            // 南京苏文软件
            if (Constants.NJSUWEN.equalsIgnoreCase(cmsSys)) {
                CLASS_URL = cmsBaseURL + "public/kebiaoall.aspx";
                GRADE_URL = cmsBaseURL + "student/chengji.aspx";
                CLASS_URL_PATTERN = "班级课表查询";
                GRADE_URL_PATTERN = "课程成绩查询";
            }
            // 正方教务系统 2012
            else if (Constants.ZFSOFT2012.equalsIgnoreCase(cmsSys)) {
                CLASS_URL_PATTERN = "学生个人课表";
                GRADE_URL_PATTERN = "平时成绩查询";
            }
            // 正方教务系统 2008
            else if (Constants.ZFSOFT2008.equalsIgnoreCase(cmsSys)) {
                CLASS_URL_PATTERN = "课程介绍查询";
                GRADE_URL_PATTERN = "学生成绩查询";
            }
            // 湖南强智科技
            else if (Constants.QZDATASOFT.equalsIgnoreCase(cmsSys)) {
                GRADE_URL_PATTERN = "课程成绩查询";
                CLASS_URL = cmsBaseURL + "jsxsd/xskb/xskb_list.do";
                GRADE_URL = cmsBaseURL + "jsxsd/kscj/cjcx_list";
            }
        }
    }

}
