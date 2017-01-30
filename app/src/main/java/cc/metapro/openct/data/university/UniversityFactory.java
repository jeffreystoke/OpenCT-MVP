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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormHandler;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;
import cc.metapro.openct.utils.SchoolInterceptor;
import okhttp3.ResponseBody;
import retrofit2.Response;

@Keep
public abstract class UniversityFactory {

    private static final String LOGIN_SUCCESS_PATTERN = "(当前)|(个人)";
    private static SchoolInterceptor interceptor;
    static URLFactory urlFactory;
    static UniversityService mService;

    UniversityInfo.CMSInfo mCMSInfo;
    CmsFactory.ClassTableInfo mClassTableInfo;
    CmsFactory.GradeTableInfo mGradeTableInfo;

    UniversityInfo.LibraryInfo mLibraryInfo;
    LibraryFactory.BorrowTableInfo mBorrowTableInfo;
    
    private boolean loginSuccess;

    @Nullable
    String login(@NonNull Map<String, String> loginMap) throws Exception {
        checkService();
        // 准备监听获取登录页面
        interceptor.setObserver(new SchoolInterceptor.redirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                // 获取登录页面后, 若是动态地址则发生302重定向, 更新LoginRefer地址 (默认是登录地址)
                urlFactory.setLoginPageURL(x);
            }
        });

        String loginPageHtml = mService.getPage(urlFactory.getBaseURL(), null).execute().body();

        // 准备监听登录结果, 若成功则会 302 跳转用户中心
        loginSuccess = false;
        interceptor.setObserver(new SchoolInterceptor.redirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                urlFactory.setUserCenterURL(x);
                loginSuccess = true;
            }
        });

        String userCenter;

        // 教务网登录
        if (mCMSInfo != null) {
            // 强智教务系统 (特殊处理)
            if (Constants.QZDATASOFT.equalsIgnoreCase(mCMSInfo.mCmsSys)) {
                String serverResponse = mService.login(urlFactory.getBaseURL() + "Logon.do?method=logon&flag=sess", getBaseURL(), new HashMap<String, String>(0)).execute().body();
                // 加密登录 (模拟网页JS代码)
                String scode = serverResponse.split("#")[0];
                String sxh = serverResponse.split("#")[1];
                String code = loginMap.get(Constants.USERNAME_KEY) + "%%%" + loginMap.get(Constants.PASSWORD_KEY);
                String encoded = "";
                for (int i = 0; i < code.length(); i++) {
                    if (i < 20) {
                        encoded = encoded + code.substring(i, i + 1) + scode.substring(0, Integer.parseInt(sxh.substring(i, i + 1)));
                        scode = scode.substring(Integer.parseInt(sxh.substring(i, i + 1)), scode.length());
                    } else {
                        encoded = encoded + code.substring(i, code.length());
                        i = code.length();
                    }
                }
                Map<String, String> map = new HashMap<>(3);
                map.put("useDogCode", "");
                map.put("encoded", encoded);
                map.put("RANDOMCODE", loginMap.get(Constants.CAPTCHA_KEY));
                Document document = Jsoup.parse(loginPageHtml, urlFactory.getLoginPageURL());
                String action = document.select("form").get(0).absUrl("action");
                userCenter = mService.login(action, urlFactory.getLoginPageURL(), map).execute().body();
                if (!TextUtils.isEmpty(userCenter) && Pattern.compile(LOGIN_SUCCESS_PATTERN).matcher(userCenter).find()) {
                    return userCenter;
                } else {
                    throw new Exception("登录失败, 请检查您的用户名和密码\n(以及验证码)");
                }
            }
        }

        FormHandler handler = new FormHandler(loginPageHtml, urlFactory.getLoginPageURL());
        Form form = handler.getForm(0);
        if (form == null) {
            throw new Exception("学校服务器好像出了点问题~\n要不等下再试试?");
        }
        Map<String, String> res = FormUtils.getLoginFiledMap(form, loginMap, true);
        String action = res.get(Constants.ACTION_KEY);
        res.remove(Constants.ACTION_KEY);
        Response<String> stringResponse = mService.login(action, urlFactory.getLoginPageURL(), res).execute();
        userCenter = stringResponse.body();

        // 处理五秒防刷
        if (userCenter.length() < 100) {
            Thread.sleep(6 * 1000);
            userCenter = mService.login(action, urlFactory.getLoginPageURL(), res).execute().body();
        }

        // 登录完成, 检测结果
        if (loginSuccess || (!TextUtils.isEmpty(userCenter) && Pattern.compile(LOGIN_SUCCESS_PATTERN).matcher(userCenter).find())) {
            return userCenter;
        } else {
            throw new Exception("登录失败, 请检查您的用户名和密码\n(以及验证码)");
        }
    }

    public void getCAPTCHA() throws IOException {
        destroyService();
        checkService();
        interceptor.setObserver(new SchoolInterceptor.redirectObserver<String>() {
            @Override
            public void onRedirect(String x) {
                // 获取登录页面后, 若是动态地址则发生302重定向, 更新LoginRefer地址 (默认是登录地址)
                urlFactory.setLoginPageURL(x);
            }
        });

        String loginPageHtml = mService.getPage(urlFactory.getBaseURL(), null).execute().body();

        // 从登录页面解析出验证码图片地址
        urlFactory.setCaptchaURL(loginPageHtml);
        Response<ResponseBody> bodyResponse = mService.getCAPTCHA(urlFactory.getCaptchaURL()).execute();
        ResponseBody body = bodyResponse.body();
        StoreHelper.storeBytes(Constants.CAPTCHA_FILE, body.byteStream());
    }

    void checkService() {
        if (interceptor == null) {
            urlFactory = new URLFactory(getBaseURL());
            interceptor = new SchoolInterceptor(urlFactory.getBaseURL());
            mService = interceptor.createSchoolService();
        }
    }

    void destroyService() {
        interceptor = null;
        mService = null;
        urlFactory = null;
    }

    protected abstract String getBaseURL();

}
