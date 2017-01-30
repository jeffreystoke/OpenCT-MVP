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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class URLFactory {

    private String baseURL;
    private String captchaURL;
    private String loginPageURL;
    private String userCenterURL;

    URLFactory(String baseURL) {
        this.baseURL = baseURL;
        loginPageURL = baseURL;
    }

    String getBaseURL() {
        return baseURL;
    }

    String getCaptchaURL() {
        return captchaURL;
    }

    void setCaptchaURL(String loginPage) {
        Document document = Jsoup.parse(loginPage, loginPageURL);
        Element codeImg = document.select("img[src~=(?i)\\.(aspx)|(asp)|(servlet)|(php)|(html)]").first();
        if (codeImg != null) {
            captchaURL = codeImg.absUrl("src");
        } else {
            codeImg = document.select("iframe[src~=(?i)\\.(aspx)|(asp)|(servlet)|(php)|(html)]").first();
            captchaURL = codeImg.absUrl("src");
        }
    }

    String getLoginPageURL() {
        return loginPageURL;
    }

    void setLoginPageURL(String loginPageURL) {
        this.loginPageURL = loginPageURL;
        // 设置默认用户中心首页为登录页, 防止未跳转的情况
        userCenterURL = loginPageURL;
    }

    String getUserCenterURL() {
        return userCenterURL;
    }

    void setUserCenterURL(String userCenterURL) {
        this.userCenterURL = userCenterURL;
    }
}
