package cc.metapro.openct;

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

import java.util.Map;

public class LoginConfig {

    private String loginURL;
    private String captchaURL;
    private String extraLoginPartURL;
    private String script;

    private Map<String, String> postHeaderSpec;
    private String postReferer;
    private String postURL;

    String getPostContentScript() {
        return String.format("function %s(username, password, extraPart) { %s }", ScriptHelper.FUNCTION_NAME, script);
    }

    public String getLoginURL() {
        return loginURL;
    }

    public String getCaptchaURL() {
        return captchaURL;
    }

    public String getExtraLoginPartURL() {
        return extraLoginPartURL;
    }

    public Map<String, String> getPostHeaderSpec() {
        return postHeaderSpec;
    }

    public String getPostReferer() {
        return postReferer;
    }

    public String getPostURL() {
        return postURL;
    }

    public boolean needCaptcha() {
        return !TextUtils.isEmpty(captchaURL);
    }

    public boolean needHeaderSpec() {
        return postHeaderSpec != null && !postHeaderSpec.isEmpty();
    }

    public boolean needExtraLoginPart() {
        return !TextUtils.isEmpty(extraLoginPartURL);
    }
}
