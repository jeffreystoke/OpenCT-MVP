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

import java.util.HashMap;
import java.util.Map;

public class LoginConfig {

    private String loginURL;
    private String captchaURL;
    private String extraLoginPartURL;
    private String fetchExtraMethod;
    private String[] script;

    private Map<String, String> postKeyValueSpec;
    private String postReferer;
    private String postURL;

    private transient String username, password, captcha, extraPart;

    public void setInfo(String username, String password, String captcha) {
        this.username = username;
        this.password = password;
        this.captcha = captcha;
    }

    public void setExtraPart(String extraPart) {
        this.extraPart = extraPart;
    }

    String getPostContentScript() {
        return String.format(ScriptHelper.FUNCTION_STRUCTURE,
                ScriptHelper.FUNCTION_NAME, TextUtils.join("\n", script));
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

    public String getFetchExtraMethod() {
        return fetchExtraMethod;
    }

    public Map<String, String> getPostKeyValueSpec() {
        Map<String, String> realHeader = new HashMap<>(0);
        if (postKeyValueSpec != null) {
            for (String s : postKeyValueSpec.keySet()) {
                String value = postKeyValueSpec.get(s);
                if (value.equalsIgnoreCase(ScriptHelper.POST_CONTENT)) {
                    String content = ScriptHelper.getPostContent(this, username, password, captcha, extraPart);
                    realHeader.put(s, content);
                } else if (value.equalsIgnoreCase(ScriptHelper.CAPTCHA)) {
                    realHeader.put(s, captcha);
                } else if (value.equalsIgnoreCase(ScriptHelper.PASSWORD)) {
                    realHeader.put(s, password);
                } else if (value.equalsIgnoreCase(ScriptHelper.USERNAME)) {
                    realHeader.put(s, username);
                } else if (value.equalsIgnoreCase(ScriptHelper.EXTRA_PART)) {
                    realHeader.put(s, extraPart);
                } else {
                    realHeader.put(s, value);
                }
            }
        }
        return realHeader;
    }

    public String getPostReferer() {
        return postReferer;
    }

    public String getViewState() {
        return null;
    }

    public String getPostURL() {
        return postURL;
    }

    public boolean needCaptcha() {
        return !TextUtils.isEmpty(captchaURL);
    }

    public boolean needHeaderSpec() {
        return postKeyValueSpec != null && !postKeyValueSpec.isEmpty();
    }

    public boolean needExtraLoginPart() {
        return !TextUtils.isEmpty(extraLoginPartURL);
    }

    public boolean isEmpty() {
        return script == null || script.length == 0;
    }
}
