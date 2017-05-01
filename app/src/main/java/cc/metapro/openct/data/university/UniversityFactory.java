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
import android.text.TextUtils;
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.LoginConfig;
import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.data.source.remote.RemoteSource;
import cc.metapro.openct.utils.TextHelper;
import cc.metapro.openct.utils.interceptors.SchoolInterceptor;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormHandler;
import cc.metapro.openct.utils.webutils.FormUtils;
import okhttp3.ResponseBody;
import retrofit2.Response;

import static cc.metapro.openct.utils.Constants.ACTION_KEY;
import static cc.metapro.openct.utils.Constants.CAPTCHA_FILE;
import static cc.metapro.openct.utils.Constants.CAPTCHA_KEY;
import static cc.metapro.openct.utils.Constants.PASSWORD_KEY;
import static cc.metapro.openct.utils.Constants.TYPE_CMS;
import static cc.metapro.openct.utils.Constants.TYPE_LIB;
import static cc.metapro.openct.utils.Constants.USERNAME_KEY;

public abstract class UniversityFactory {

    private static final Pattern LOGIN_SUCCESS_PATTERN = Pattern.compile("(当前)|(个人)|(退出)|(注销)");
    static UniversityService mService;
    static String SYS;

    private static LoginConfig mLoginConfig;
    private static WebHelper webHelper;
    private static SchoolInterceptor interceptor;
    private static String BASE_URL;

    private RemoteSource mRemoteRepoSource;
    private String mUsername, mPassword, mCaptcha;
    private boolean loginSuccess;

    UniversityFactory(UniversityInfo info, int type) {
        mRemoteRepoSource = new RemoteSource(info.getName());
        if (type == TYPE_CMS) {
            SYS = info.getCmsSys();
            BASE_URL = URLUtil.guessUrl(info.getCmsURL());
        } else if (type == TYPE_LIB) {
            SYS = info.getLibSys();
            BASE_URL = URLUtil.guessUrl(info.getLibURL());
        }
    }

    // 再次获取验证码
    public static void getOneMoreCAPTCHA() throws IOException {
        Response<ResponseBody> bodyResponse = mService.getPic(webHelper.getCaptchaURL()).execute();
        ResponseBody body = bodyResponse.body();
        StoreHelper.storeBytes(CAPTCHA_FILE, body.byteStream());
    }

    @NonNull
    public Document login(@NonNull final Map<String, String> loginMap) throws Exception {
        // 准备监听登录结果, 若成功会 302 跳转用户中心
        loginSuccess = false;
        interceptor.setObserver(new SchoolInterceptor.RedirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                webHelper.setUserCenterURL(x);
                loginSuccess = true;
            }
        });

        mUsername = loginMap.get(USERNAME_KEY);
        mPassword = loginMap.get(PASSWORD_KEY);
        mCaptcha = loginMap.get(CAPTCHA_KEY);

        Exception LOGIN_FAIL = new Exception("登录失败, 请检查您的用户名和密码\n" + "(以及验证码)");
        String USER_CENTER = "";

        if (mLoginConfig != null && !mLoginConfig.isEmpty()) {
            return fineLogin();
        }

        // common login
        Document loginPageDom = webHelper.getLoginPageDOM();
        if (loginPageDom != null) {
            FormHandler formHandler = new FormHandler(loginPageDom);
            Form targetForm = webHelper.getLoginForm() == null ? formHandler.getForm(0) : new Form(webHelper.getLoginForm());
            if (targetForm == null) {
                throw new Exception("观测到学校服务器出了点问题~");
            }

            Map<String, String> res = FormUtils.getLoginFiledMap(targetForm, loginMap, true);
            String action = res.get(ACTION_KEY);
            res.remove(ACTION_KEY);
            Response<String> stringResponse = mService.post(action, res).execute();
            USER_CENTER = stringResponse.body();

            // beat 5 seconds restriction
            if (USER_CENTER.length() < 100) {
                Thread.sleep(6 * 1000);
                USER_CENTER = mService.post(action, res).execute().body();
            }

            Document document = Jsoup.parse(USER_CENTER, webHelper.getUserCenterURL());
            Elements frames = document.select("frame");
            frames.addAll(document.select("iframe"));
            for (Element frame : frames) {
                String url = frame.absUrl("src");
                if (!TextUtils.isEmpty(url)) {
                    document.append(mService.get(url).execute().body());
                }
            }
            // fineLogin finish, check results
            if (loginSuccess ||
                    (!TextUtils.isEmpty(USER_CENTER) &&
                            LOGIN_SUCCESS_PATTERN.matcher(USER_CENTER).find())) {
                return document;
            } else {
                throw LOGIN_FAIL;
            }
        }

        return Jsoup.parse(USER_CENTER, webHelper.getUserCenterURL());
    }

    private Document fineLogin() throws IOException {

        mLoginConfig.setInfo(mUsername, mPassword, mCaptcha);
        // get extra part
        if (mLoginConfig.needExtraLoginPart()) {
            String extraPartUrl = webHelper.getLoginPageURL()
                    .newBuilder(mLoginConfig.getExtraLoginPartURL())
                    .toString();
            String extraPart = "";
            String method = TextUtils.isEmpty(mLoginConfig.getFetchExtraMethod()) ?
                    "GET" : mLoginConfig.getFetchExtraMethod().toUpperCase();
            if (method.equals("POST")) {
                extraPart = mService.post(extraPartUrl, new HashMap<String, String>(0)).execute().body();
            } else if (method.equals("GET")) {
                extraPart = mService.get(extraPartUrl).execute().body();
            }
            mLoginConfig.setExtraPart(extraPart);
        }

        // get form action url
        String action;
        if (!TextUtils.isEmpty(mLoginConfig.getPostURL())) {
            action = mLoginConfig.getPostURL();
        } else {
            action = UniversityUtils.getLoginFormAction(webHelper);
        }

        Map<String, String> keyValueSpec = mLoginConfig.getPostKeyValueSpec();
        String userCenter = mService.post(action, keyValueSpec).execute().body();
        return Jsoup.parse(userCenter, webHelper.getUserCenterURL());
    }

    // 初次获取验证码
    public boolean prepareOnlineInfo() throws IOException {
        destroyService();
        checkService();

        // 获取登录页面后, 若是动态地址则发生302重定向, 更新 LoginRefer 以及 LoginPage 地址 (默认是登录地址)
        interceptor.setObserver(new SchoolInterceptor.RedirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                webHelper.setLoginPageURL(x);
            }
        });

        // 获取精确配置
        mLoginConfig = mRemoteRepoSource.getLoginConfig();
        String loginUrl = "";
        if (mLoginConfig != null) {
            loginUrl = TextHelper.getFirstWhenNotEmpty(mLoginConfig.getLoginURL(), webHelper.getLoginPageURL().toString());
        }

        String loginPageHtml = mService.get(loginUrl).execute().body();
        webHelper.parseCaptchaURL(SYS, loginPageHtml, mService);

        // 获取验证码图片, 精确配置的优先级高于自动解析
        String captchaURL = webHelper.getCaptchaURL();
        if (mLoginConfig != null && !mLoginConfig.isEmpty()) {
            if (mLoginConfig.needCaptcha()) {
                captchaURL = webHelper.getLoginPageURL()
                        .newBuilder(mLoginConfig.getCaptchaURL()).toString();
                ResponseBody body = mService.getPic(captchaURL).execute().body();
                StoreHelper.storeBytes(CAPTCHA_FILE, body.byteStream());
            }
            return true;
        } else if (!TextUtils.isEmpty(captchaURL)) {
            ResponseBody body = mService.getPic(captchaURL).execute().body();
            StoreHelper.storeBytes(CAPTCHA_FILE, body.byteStream());
            return true;
        }

        return false;
    }

    void checkService() {
        if (webHelper == null) {
            webHelper = new WebHelper(getBaseURL());
            interceptor = webHelper.getInterceptor();
            mService = webHelper.createSchoolService();
        }
    }

    private void destroyService() {
        interceptor = null;
        mService = null;
        webHelper = null;
        mLoginConfig = null;
    }

    protected String getBaseURL() {
        return BASE_URL;
    }

}
