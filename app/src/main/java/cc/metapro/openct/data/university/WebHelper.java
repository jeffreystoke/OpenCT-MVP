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
import android.webkit.URLUtil;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.List;

import cc.metapro.openct.utils.Constants;
import okhttp3.HttpUrl;

class WebHelper {

    private HttpUrl mBaseUrl;
    private HttpUrl mCaptchaUrl;
    private HttpUrl mLoginPageUrl;
    private HttpUrl mUserCenterUrl;
    private Element loginForm;
    private Document loginPageDOM;

    WebHelper(String baseURL) {
        mBaseUrl = HttpUrl.parse(URLUtil.guessUrl(baseURL));
        mLoginPageUrl = mBaseUrl;
        mUserCenterUrl = mBaseUrl;
    }

    String getBaseURL() {
        return mBaseUrl.toString();
    }

    String getCaptchaURL() {
        return mCaptchaUrl == null ? null : mCaptchaUrl.toString();
    }

    void setCaptchaURL(List<Document> documents, String system) {
        loop:
        for (Document document : documents) {
            loginPageDOM = document;
            mLoginPageUrl = HttpUrl.parse(document.baseUri());
            Elements forms = document.select("form");

            // 遍历表单
            for (Element form : forms) {
                Elements codeImg = form.select("img[src~=(?i)^(?!.*(png|jpg|gif|ico)).*$");
                codeImg.addAll(form.select("iframe[src~=(?i)^(?!.*(png|jpg|gif|ico)).*$"));
                if (Constants.KINGOSOFT.equalsIgnoreCase(system)) {
                    Element captcha = new Element(Tag.valueOf("img"), document.baseUri());
                    captcha.attr("src", "../sys/ValidateCode.aspx");
                    codeImg.add(captcha);
                    form.appendChild(form.parent().after(form));
                }
                // 获取验证码地址
                for (Element img : codeImg) {
                    String url = img.absUrl("src");
                    if (!TextUtils.isEmpty(url)) {
                        loginForm = form;
                        mCaptchaUrl = HttpUrl.parse(URLUtil.guessUrl(url));
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
        return mLoginPageUrl.toString();
    }

    void setLoginPageURL(String loginPageURL) {
        mLoginPageUrl = mLoginPageUrl.newBuilder(loginPageURL).build();
        // 设置默认用户中心首页为登录页, 防止未跳转的情况
        mUserCenterUrl = mLoginPageUrl;
    }

    String getUserCenterURL() {
        return mUserCenterUrl.toString();
    }

    void setUserCenterURL(String userCenterURL) {
        mUserCenterUrl = mUserCenterUrl.newBuilder(userCenterURL).build();
    }
}
