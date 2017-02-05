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

import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

class WebHelper {

    private String baseURL;
    private String captchaURL;
    private String loginPageURL;
    private String userCenterURL;
    private Element loginForm;
    private Document loginPageDOM;

    WebHelper(String baseURL) {
        this.baseURL = baseURL;
        loginPageURL = baseURL;
    }

    String getBaseURL() {
        return baseURL;
    }

    String getCaptchaURL() {
        return captchaURL;
    }

    void setCaptchaURL(List<Document> documents) {
        loop:
        for (Document document : documents) {
            loginPageDOM = document;
            loginPageURL = document.baseUri();
            Elements forms = document.select("form");

            // 遍历表单
            for (Element form : forms) {
                Elements codeImg = form.select("img[src~=(?i)^(?!.*\\.(png|jpg|gif|ico)).*$");
                codeImg.addAll(form.select("iframe[src~=(?i)^(?!.*\\.(png|jpg|gif|ico)).*$"));

                // 获取验证码地址
                for (Element img : codeImg) {
                    String url = img.absUrl("src");
                    if (!TextUtils.isEmpty(url)) {
                        loginForm = form;
                        captchaURL = url;
                        break loop;
                    }
                }
            }
        }
    }

    Document getLoginPageDOM() {
        return loginPageDOM;
    }

    Element getLoginForm() {
        return loginForm;
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
