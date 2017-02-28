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
import android.webkit.URLUtil;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.interceptors.SchoolInterceptor;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormHandler;
import cc.metapro.openct.utils.webutils.FormUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

@Keep
public abstract class UniversityFactory {

    private static final String LOGIN_SUCCESS_PATTERN = "(当前)|(个人)";
    static UniversityService mService;
    static String SYS;
    private static WebHelper webHelper;
    private static SchoolInterceptor interceptor;
    private static String BASE_URL;

    private boolean loginSuccess;

    UniversityFactory(String sys, String url) {
        SYS = sys;
        BASE_URL = URLUtil.guessUrl(url);
    }

    // 再次获取验证码
    public static void getOneMoreCAPTCHA() throws IOException {
        Response<ResponseBody> bodyResponse = mService.getCAPTCHA(webHelper.getCaptchaURL()).execute();
        ResponseBody body = bodyResponse.body();
        StoreHelper.storeBytes(Constants.CAPTCHA_FILE, body.byteStream());
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

        Exception LOGIN_FAIL = new Exception("登录失败, 请检查您的用户名和密码\n" + "(以及验证码)");

        String USER_CENTER = "";

        // 强智教务系统 (特殊处理)
        if (Constants.QZDATASOFT.equalsIgnoreCase(SYS)) {
            // 无需验证码则没有加密 -> 使用普通方法
            if (webHelper.getLoginForm() != null) {
                String urlTmp = webHelper.getBaseURL()
                        .newBuilder("/Logon.do?method=logon&flag=sess")
                        .build().toString();

                final String serverResponse = mService.post(urlTmp, new HashMap<String, String>(0)).execute().body();

                String action = webHelper.getLoginForm().absUrl("action");

                USER_CENTER = mService.post(action, new LinkedHashMap<String, String>() {{
                    put("useDogCode", "");
                    put("encoded", UniversityUtils.QZEncryption(serverResponse, loginMap));
                    put("RANDOMCODE", loginMap.get(Constants.CAPTCHA_KEY));
                }}).execute().body();
                if (loginSuccess || (!TextUtils.isEmpty(USER_CENTER) && Pattern.compile(LOGIN_SUCCESS_PATTERN).matcher(USER_CENTER).find())) {
                    return Jsoup.parse(USER_CENTER, webHelper.getUserCenterURL());
                } else {
                    throw LOGIN_FAIL;
                }
            }
        }

        Document loginPageDom = webHelper.getLoginPageDOM();
        if (loginPageDom != null) {
            FormHandler formHandler = new FormHandler(loginPageDom);
            Form targetForm = webHelper.getLoginForm() == null ? formHandler.getForm(0) : new Form(webHelper.getLoginForm());
            if (targetForm == null) {
                throw new Exception("观测到学校服务器出了点问题~");
            }

            Map<String, String> res = FormUtils.getLoginFiledMap(targetForm, loginMap, true);
            if (Constants.KINGOSOFT.equalsIgnoreCase(SYS)) {
                res.put("pcInfo", SchoolInterceptor.userAgent);
                res.put("typeName", "学生");
                res.put("txt_sdertfgsadscxcadsads", loginMap.get(Constants.CAPTCHA_KEY));
                res.remove("sbtState");
            }

            String action = res.get(Constants.ACTION_KEY);
            res.remove(Constants.ACTION_KEY);
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
                    document.append(mService.getPage(url).execute().body());
                }
            }
            // login finish, check results
            if (loginSuccess || (!TextUtils.isEmpty(USER_CENTER) && Pattern.compile(LOGIN_SUCCESS_PATTERN).matcher(USER_CENTER).find())) {
                return document;
            } else {
                throw LOGIN_FAIL;
            }
        }

        return Jsoup.parse(USER_CENTER, webHelper.getUserCenterURL());
    }

    // 初次获取验证码
    public boolean prepareOnlineInfo() throws IOException {
        destroyService();
        checkService();
        interceptor.setObserver(new SchoolInterceptor.RedirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                // 获取登录页面后, 若是动态地址则发生302重定向, 更新LoginRefer地址 (默认是登录地址)
                webHelper.setLoginPageURL(x);
            }
        });

        Call<String> call = mService.getPage(webHelper.getBaseURL().toString());
        Response<String> stringResponse = call.execute();
        String loginPageHtml = stringResponse.body();

        Document document = Jsoup.parse(loginPageHtml, webHelper.getLoginPageURL());
        List<Document> domList = new ArrayList<>();
        domList.add(document);

        // 获取框架网页 (部分学校将登陆表单置于框架中)
        Elements iFrames = document.select("iframe");
        iFrames.addAll(document.select("span:matches(登录)"));
        for (Element iFrame : iFrames) {
            String url = iFrame.attr("src");
            if (TextUtils.isEmpty(url)) {
                url = iFrame.absUrl("value");
            } else {
                url = iFrame.absUrl("src");
            }
            if (!TextUtils.isEmpty(url)) {
                String frame = mService.getPage(url).execute().body();
                domList.add(Jsoup.parse(frame, url));
            }
        }

        // 从所有获取到的页面中解析出验证码图片地址
        webHelper.setCaptchaURL(domList, SYS);

        // 获取验证码
        String captchaURL = webHelper.getCaptchaURL();
        if (!TextUtils.isEmpty(captchaURL)) {
            Response<ResponseBody> bodyResponse = mService.getCAPTCHA(captchaURL).execute();
            ResponseBody body = bodyResponse.body();
            StoreHelper.storeBytes(Constants.CAPTCHA_FILE, body.byteStream());
            return true;
        } else {
            return false;
        }
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
    }

    protected String getBaseURL() {
        return BASE_URL;
    }

}
