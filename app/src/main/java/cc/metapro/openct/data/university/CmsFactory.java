package cc.metapro.openct.data.university;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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

import com.google.common.base.Strings;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import cc.metapro.openct.data.university.UniversityInfo.CMSInfo;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.PageStringUtils;

public class CmsFactory extends UniversityFactory {

    private CmsURLFactory mURLFactory;

    public CmsFactory(UniversityService service, CMSInfo cmsInfo) {
        mCMSInfo = cmsInfo;
        mService = service;
        mClassTableInfo = cmsInfo.mClassTableInfo;
        mGradeTableInfo = cmsInfo.mGradeTableInfo;

        if (!cmsInfo.mCmsURL.endsWith("/")) {
            cmsInfo.mCmsURL += "/";
        }
        mURLFactory = new CmsURLFactory(cmsInfo.mCmsSys, cmsInfo.mCmsURL);
    }

    /**
     * 解析用户中心页面时提供的 BaseURL 由 URLFactory 生成
     * 即使用 mURLFactory.LOGIN_URL 作为页面解析的 BaseURL
     *
     * @param loginMap - 教务网登录信息, 包含用户名, 密码, 验证码(如果需要的话)
     * @return 解析完成的课程信息
     * @throws IOException
     * @throws LoginException
     */
    @NonNull
    public List<ClassInfo> getClasses(Map<String, String> loginMap) throws Exception {
        String page = login(loginMap);
        String tableURL = null;
        String tablePage = null;
        // 苏文
        if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.NJSUWEN) || mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.QZDATASOFT)) {
            tablePage = mService.getPage(mURLFactory.CLASS_URL, mURLFactory.LOGIN_URL).execute().body();
            if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
            return UniversityFactory.generateClasses(tablePage.replaceAll("◇", Constants.BR_REPLACER), mClassTableInfo);
        }
        // 强智
        else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.QZDATASOFT)) {
            // TODO: 17/1/15 获取强智成绩
//            if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
//            return UniversityFactory.generateClasses(tablePage.replaceAll("<br>|-{3,}", Constants.BR_REPLACER), mClassTableInfo);
        }
        // 正方2012
        else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.ZFSOFT2012)) {
            Document doc = Jsoup.parse(page, mURLFactory.LOGIN_URL);
            Elements addresses = doc.select("a");
            for (Element e : addresses) {
                if ("GetMc('学生个人课表');".equals(e.attr("onclick"))) {
                    tableURL = e.absUrl("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
            tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
            if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
            return UniversityFactory.generateClasses(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER), mClassTableInfo);
        }
        // 正方2008
        else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.ZFSOFT2008)) {
            Document doc = Jsoup.parse(page, mURLFactory.LOGIN_URL);
            Elements addresses = doc.select("a");
            for (Element e : addresses) {
                if ("GetMc('课程介绍查询');".equals(e.attr("onclick"))) {
                    tableURL = e.absUrl("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
            tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
            if (!Strings.isNullOrEmpty(tablePage)) {
                // TODO: 17/1/15 获取正方2008版成绩
            } else {
                return new ArrayList<>(0);
            }
            return UniversityFactory.generateClasses(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER), mClassTableInfo);
        }
        // 未支持的院校
        return new ArrayList<>(0);
    }

    /**
     * tend to get grade info page
     *
     * @param loginMap - cms user info
     * @return a list of grade info
     * @throws IOException
     * @throws LoginException
     */
    @NonNull
    public List<GradeInfo> getGrades(Map<String, String> loginMap) throws Exception {
        String page = login(loginMap);
        String tableURL = null;
        String tablePage = null;
        if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.NJSUWEN)) {
            tablePage = mService.getPage(mURLFactory.GRADE_URL, mCMSInfo.mCmsURL).execute().body();
        }
        // 正方2012
        else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.ZFSOFT2012)) {
            Document doc = Jsoup.parse(page, mCMSInfo.mCmsURL);
            Elements ele = doc.select("a");
            for (Element e : ele) {
                if ("GetMc('平时成绩查询');".equals(e.attr("onclick"))) {
                    tableURL = mCMSInfo.mCmsURL + e.attr("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
            tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();
        }
        // 正方2008
        else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.ZFSOFT2008)) {
            Document doc = Jsoup.parse(page, mCMSInfo.mCmsURL);
            Elements ele = doc.select("a");
            for (Element e : ele) {
                if ("GetMc('学生成绩查询');".equals(e.attr("onclick"))) {
                    tableURL = mCMSInfo.mCmsURL + e.attr("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(tableURL)) return new ArrayList<>(0);
            tablePage = mService.getPage(tableURL, mCMSInfo.mCmsURL).execute().body();

        } else if (mCMSInfo.mCmsSys.equalsIgnoreCase(Constants.QZDATASOFT)) {
            mService.getPage(mCMSInfo.mCmsURL + "jsxsd/framework/blankPage.jsp", mURLFactory.USERCENTER_URL).execute();
            Document document = Jsoup.parse(page, mCMSInfo.mCmsURL);
            Elements links = document.select("a");
            String queryURL = null;
            for (Element e : links) {
                if ("课程成绩查询".equals(e.text())) {
                    queryURL = e.absUrl("href");
                    break;
                }
            }
            if (Strings.isNullOrEmpty(queryURL)) {
                return new ArrayList<>(0);
            }
            String queryPage = mService.getPage(queryURL, mURLFactory.USERCENTER_URL).execute().body();
            document = Jsoup.parse(queryPage);
            String kksj = document.select("form").get(0).select("option").get(2).attr("value");
            Map<String, String> postMap = new HashMap<>(3);
            postMap.put("kksj", kksj);
            postMap.put("kcxz", "");
            postMap.put("kcmc", "");
            postMap.put("xsfs", "all");
            tablePage = mService.post(queryURL, queryURL, postMap).execute().body();
        }
        if (Strings.isNullOrEmpty(tablePage)) return new ArrayList<>(0);
        return UniversityFactory.generateGrades(PageStringUtils.replaceAllBrWith(tablePage, Constants.BR_REPLACER), mGradeTableInfo);
    }

    @Override
    protected String
    getCaptchaURL() {
        return mURLFactory.CAPTCHA_URL;
    }

    @Override
    protected String
    getLoginURL() {
        return mURLFactory.LOGIN_URL;
    }

    @Override
    protected String
    getLoginRefer() {
        return mURLFactory.LOGIN_REF;
    }

    @Override
    protected void resetURLFactory() {
        mURLFactory = new CmsURLFactory(mCMSInfo.mCmsSys, mCMSInfo.mCmsURL + "/" + dynPart + "/");
    }

    public static class GradeTableInfo {
        public int
                mClassCodeIndex,
                mClassNameIndex,
                mClassTypeIndex,
                mPointsIndex,
                mGradeSummaryIndex,
                mGradePracticeIndex,
                mGradeCommonIndex,
                mGradeMidExamIndex,
                mGradeFinalExamIndex,
                mGradeMakeupIndex;

        public String mGradeTableID;
    }

    public static class ClassTableInfo {

        public int
                mDailyClasses,
                mNameIndex,
                mTypeIndex,
                mDuringIndex,
                mPlaceIndex,
                mTimeIndex,
                mTeacherIndex,
                mClassStringCount,
                mClassLength;

        public String
                mClassTableID,
                mClassInfoStart;

        // Regular Expressions to parse class
        public String
                mNameRE, mTypeRE,
                mDuringRE, mTimeRE,
                mTeacherRE, mPlaceRE;
    }

    private static class CmsURLFactory {
        String
                LOGIN_URL, LOGIN_REF,
                CLASS_URL, GRADE_URL,
                CAPTCHA_URL, USERCENTER_URL;

        CmsURLFactory(@NonNull String cmsSys, @NonNull String cmsBaseURL) {
            // 南京苏文软件
            if (Constants.NJSUWEN.equalsIgnoreCase(cmsSys)) {
                LOGIN_URL = cmsBaseURL;
                LOGIN_REF = cmsBaseURL;
                CLASS_URL = cmsBaseURL + "public/kebiaoall.aspx";
                GRADE_URL = cmsBaseURL + "student/chengji.aspx";
                CAPTCHA_URL = cmsBaseURL + "yzm.aspx";
            }
            // 正方教务系统
            else if (Constants.ZFSOFT2012.equalsIgnoreCase(cmsSys) || Constants.ZFSOFT2008.equalsIgnoreCase(cmsSys)) {
                LOGIN_URL = cmsBaseURL;
                LOGIN_REF = cmsBaseURL;
                CAPTCHA_URL = cmsBaseURL + "CheckCode.aspx";
            }
            // 湖南强智科技
            else if (Constants.QZDATASOFT.equalsIgnoreCase(cmsSys)) {
                LOGIN_URL = cmsBaseURL;
                LOGIN_REF = cmsBaseURL;
                CAPTCHA_URL = cmsBaseURL + "verifycode.servlet";
                CLASS_URL = cmsBaseURL + "jsxsd/xskb/xskb_list.do";
                GRADE_URL = cmsBaseURL + "jsxsd/kscj/cjcx_query";
                USERCENTER_URL = cmsBaseURL + "jsxsd/framework/xsMain.jsp";
            }
        }
    }

}
