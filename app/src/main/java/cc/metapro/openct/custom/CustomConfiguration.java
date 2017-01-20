package cc.metapro.openct.custom;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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

public class CustomConfiguration {

    private String preLoginJS;
    private String postLoginJS;
    private String loginButtonJS;
    private boolean needCaptcha;
    private boolean needLogin;

    public CustomConfiguration() {

    }

    public CustomConfiguration(String preLoginJS, String postLoginJS) {
        this.preLoginJS = preLoginJS;
        this.postLoginJS = postLoginJS;
    }

    public void setNeedCaptcha(boolean needCaptcha) {
        this.needCaptcha = needCaptcha;
    }

    public void setNeedLogin(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public boolean needCaptcha() {
        return needCaptcha;
    }

    public String getPostLoginJS() {
        return postLoginJS;
    }

    public void setPostLoginJS(String postLoginJS) {
        this.postLoginJS = postLoginJS;
    }

    public String getPreLoginJS() {
        return preLoginJS;
    }

    public void setPreLoginJS(String preLoginJS) {
        this.preLoginJS = preLoginJS;
    }

    public String getLoginButtonJS() {
        return loginButtonJS;
    }

    public void setLoginButtonJS(String loginButtonJS) {
        this.loginButtonJS = loginButtonJS;
    }

    public boolean needLogin() {
        return needLogin;
    }
}
