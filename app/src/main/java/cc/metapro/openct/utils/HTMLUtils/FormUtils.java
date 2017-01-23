package cc.metapro.openct.utils.HTMLUtils;

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
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.metapro.openct.utils.Constants;

public class FormUtils {

    private final static Pattern INVISIBLE_FORM_ITEM = Pattern.compile("(DISPLAY: none)|(hidden)");

    private final static Pattern typeKey = Pattern.compile("(strSearchType)");

    private final static Pattern searchKey = Pattern.compile("(strText)");

    @NonNull
    public static Map<String, String> getLibSearchQueryMap(
            @NonNull Form form, @NonNull Map<String, String> kvs) {

        String searchType = kvs.get(Constants.SEARCH_TYPE);
        String searchContent = kvs.get(Constants.SEARCH_CONTENT);

        Map<String, String> res = new LinkedHashMap<>();
        boolean clicked = false;

        for (Elements elements : form.getFormItems().values()) {
            Element element = classify(elements, null);

            if (element == null) continue;

            String type = element.attr("type");
            String key = element.attr("name");
            String value = element.attr("value");
            if (type.equalsIgnoreCase("image")) {
                type = "submit";
                key = "x=0&y";
                value = "0";
            }
            String onclick = element.attr("onclick");
            if (typeKey.matcher(key).find()) {
                res.put(key, searchType);
            } else if ("radio".equalsIgnoreCase(type)) {
                // radio options
                res.put(key, value);
            } else if ("submit".equalsIgnoreCase(type)) {
                // submit buttons
                if (TextUtils.isEmpty(onclick) && !clicked) {
                    if (!TextUtils.isEmpty(key)) {
                        res.put(key, value);
                    }
                    clicked = true;
                }
            } else if ("text".equalsIgnoreCase(type)) {
                if (searchKey.matcher(key).find()) {
                    res.put(key, searchContent);
                }
            } else {
                res.put(key, value);
            }
        }
        res.put(Constants.ACTION, form.getAction());
        return res;
    }

    @NonNull
    public static Map<String, String> getLoginFiledMap(
            @NonNull Form form,
            @NonNull Map<String, String> kvs,
            boolean needClick
    ) {
        Elements prev = null;
        Map<String, String> loginMap = new LinkedHashMap<>();
        boolean clicked = false;
        boolean passwordOK = false;

        for (Elements elements : form.getFormItems().values()) {
            Element element = classify(elements, null);
            if (element == null) continue;
            String type = element.attr("type");
            String key = element.attr("name");
            String value = element.attr("value");
            String onclick = element.attr("onclick");
            String id = element.id();
            if ("radio".equalsIgnoreCase(type)) {
                loginMap.put(key, value);
            } else if ("submit".equalsIgnoreCase(type)) {
                // submit buttons
                if (TextUtils.isEmpty(onclick) && !clicked) {
                    if (needClick) {
                        loginMap.put(key, value);
                    }
                    clicked = true;
                }
            } else if ("password".equalsIgnoreCase(type) && prev != null) {
                // password text
                String username = kvs.get(Constants.USERNAME_KEY);
                String password = kvs.get(Constants.PASSWORD_KEY);

                // 填写用户名
                String userNameKey = prev.attr("name");
                if (TextUtils.isEmpty(userNameKey)) {
                    userNameKey = prev.attr("id");
                }
                loginMap.put(userNameKey, username);

                // 填写密码
                if (!TextUtils.isEmpty(key)) {
                    loginMap.put(key, password);
                } else {
                    loginMap.put(id, password);
                }
                passwordOK = true;
            } else if ("text".equalsIgnoreCase(type)) {
                // common text
                // secret code text (after password)
                if (prev != null && passwordOK) {
                    String code = kvs.get(Constants.CAPTCHA_KEY);
                    if (!TextUtils.isEmpty(key)) {
                        loginMap.put(key, code);
                    } else {
                        loginMap.put(id, code);
                    }
                    passwordOK = false;
                } else {
                    Matcher matcher = INVISIBLE_FORM_ITEM.matcher(elements.toString());
                    if (matcher.find()) {
                        loginMap.put(key, value);
                    }
                }
            } else {
                loginMap.put(key, value);
            }
            prev = elements;
        }
        loginMap.put(Constants.ACTION, form.getAction());
        return loginMap;
    }

    private static Element classify(@NonNull Elements elements, @Nullable String preferedValue) {
        if (elements.size() == 0) return null;
        String tagName = elements.get(0).tagName();
        if ("input".equalsIgnoreCase(tagName)) {
            String type = elements.attr("type");
            switch (type) {
                case "radio":
                    return radio(elements, preferedValue);
            }
        }
        return elements.get(0);
    }

    private static Element radio(@NonNull Elements radios, @Nullable String preferedValue) {
        for (Element r : radios) {
            if (r.hasAttr("checked")) {
                if (!TextUtils.isEmpty(preferedValue)) {
                    r = r.attr("value", preferedValue);
                }
                return r;
            }
        }
        return radios.get(0);
    }
}
