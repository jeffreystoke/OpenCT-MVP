package cc.metapro.interactiveweb;

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

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import cc.metapro.interactiveweb.htmlinterface.HTMLClicker;
import cc.metapro.interactiveweb.htmlinterface.HTMLInspector;
import cc.metapro.interactiveweb.htmlinterface.HTMLSetter;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.interactiveweb.utils.JSUtils;


public class InteractiveWebView extends WebView implements HTMLClicker, HTMLSetter, HTMLInspector {

    public static final String COMMON_INPUT_FLAG = "common_input";
    public static final String USERNAME_INPUT_FLAG = "username_input";

    private LinkedHashMap<String, Document> mVisitedPageDomMap = new LinkedHashMap<>();
    private InteractiveWebViewClient mWebViewClient = new InteractiveWebViewClient();
    private ClickCallback mUserClickCallback;

    public InteractiveWebView(Context context) {
        super(context);
        init();
    }

    public InteractiveWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public InteractiveWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        super.setWebViewClient(mWebViewClient);
        WebSettings settings = getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);

        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        addJavascriptInterface(new JsInteraction(new JsInteraction.CallBack() {
            @Override
            public void onAction(int type, String domItem) {
                String baseURL = mWebViewClient.getCurrentPageURL();
                if (JsInteraction.ON_CLICK == type) {
                    final Element element = Jsoup.parse(domItem, baseURL).body().children().first();
                    if (element != null) {
                        if (mUserClickCallback != null) {
                            mUserClickCallback.onClick(element);
                        }
                    }

                } else if (JsInteraction.ON_LOAD_SOURCE == type) {
                    domItem = domItem.replaceAll(HTMLUtils.BR, HTMLUtils.BR_REPLACER);
                    if (baseURL != null) {
                        mVisitedPageDomMap.put(baseURL, Jsoup.parse(domItem, baseURL));
                    }
                }
            }
        }), JsInteraction.INTERFACE_NAME);
    }

    public void setUserClickCallback(ClickCallback callback) {
        mUserClickCallback = callback;
    }

    @Override
    public boolean clickElementById(String id) {
        if (TextUtils.isEmpty(id)) return false;
        Element element = getElementById(id);
        if (element != null) {
            JSUtils.clickById(this, id);
            return true;
        }
        return false;
    }

    @Override
    public boolean clickElementsByName(String name) {
        if (TextUtils.isEmpty(name)) return false;
        Elements elements = getElementsByName(name);
        if (elements != null && !elements.isEmpty()) {
            JSUtils.clickByName(this, name);
            return true;
        }
        return false;
    }

    @Override
    public boolean clickElementsByTag(String tag) {
        if (TextUtils.isEmpty(tag)) return false;
        Elements elements = getElementsByTag(tag);
        if (elements != null && !elements.isEmpty()) {
            JSUtils.clickByTag(this, tag);
            return true;
        }
        return false;
    }

    @Override
    public boolean clickElementsByPattern(String pattern) {
        if (TextUtils.isEmpty(pattern)) return false;
        Document document = getPageDom();
        if (document != null) {
            Elements elements = document.getAllElements();
            Pattern ptn = Pattern.compile(pattern);
            for (Element element : elements) {
                if (ptn.matcher(element.toString()).find()) {
                    JSUtils.clickByPattern(this, pattern);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean setById(String id, String key, String value) {
        if (TextUtils.isEmpty(id)) return false;
        Document document = getPageDom();
        if (document != null) {
            if (document.getElementById(id) != null) {
                JSUtils.setById(this, id, key, value);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setByTag(String tag, String key, String value) {
        if (TextUtils.isEmpty(tag)) return false;
        Elements elements = getElementsByTag(tag);
        if (elements != null && !elements.isEmpty()) {
            JSUtils.setByTag(this, tag, key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean setByName(String name, String key, String value) {
        if (TextUtils.isEmpty(name)) return false;
        Elements elements = getElementsByAttr("name", name);
        if (elements != null && !elements.isEmpty()) {
            JSUtils.setByName(this, name, key, value);
            return true;
        }
        return false;
    }

    @Override
    public boolean setByPattern(String pattern, String key, String value) {
        if (TextUtils.isEmpty(pattern)) return false;
        Document document = getPageDom();
        if (document != null) {
            Elements elements = document.getAllElements();
            Pattern ptn = Pattern.compile(pattern);
            for (Element element : elements) {
                if (ptn.matcher(element.toString()).find()) {
                    JSUtils.setByPattern(this, pattern, key, value);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getPageSource() {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null ? "" : document.html();
    }

    @Override
    public Document getPageDom() {
        return mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
    }

    @Override
    public Element getElementById(String id) {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null || TextUtils.isEmpty(id) ? null : document.getElementById(id);
    }

    @Override
    public Elements getElementsByName(String name) {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null || TextUtils.isEmpty(name) ? null : document.getElementsByAttributeValue("name", name);
    }

    @Override
    public Elements getElementsByTag(String tag) {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null || TextUtils.isEmpty(tag) ? null : document.getElementsByTag(tag);
    }

    @Override
    public Elements getElementsByClass(String c) {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null || TextUtils.isEmpty(c) ? null : document.getElementsByClass(c);
    }

    @Override
    public Elements getElementsByAttr(String attr, String value) {
        Document document = mVisitedPageDomMap.get(mWebViewClient.getCurrentPageURL());
        return document == null || TextUtils.isEmpty(attr) ? null : document.getElementsByAttributeValue(attr, value == null ? "" : value);
    }

    public boolean focusById(String id) {
        if (TextUtils.isEmpty(id)) return false;
        Element element = getElementById(id);
        if (element != null) {
            JSUtils.focusById(this, id);
            return true;
        }
        return false;
    }

    public boolean focusByName(String name) {
        if (TextUtils.isEmpty(name)) return false;
        Elements elements = getElementsByName(name);
        if (elements != null && !elements.isEmpty()) {
            JSUtils.focusByName(this, name);
            return true;
        }
        return false;
    }

    public interface ClickCallback {
        void onClick(Element element);
    }
}
