package cc.metapro.interactiveweb.utils;

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
import android.webkit.WebView;

import cc.metapro.interactiveweb.JsInteraction;

public final class JSUtils {

    private static final String CLICK_LISTENER =
            "\"function getClick(e){" +
                    "var targ;if(!e){var e=window.event;}" +
                    "if(e.target){targ=e.target;}" +
                    "else if(e.srcElement){targ=e.srcElement;}" +
                    "if(targ.nodeType==3){targ=targ.parentNode;}" +
                    "targ.click();" +
                    "window." + JsInteraction.INTERFACE_NAME + ".onClicked(targ.outerHTML);}\"";

    public static void injectClickListener(@NonNull final WebView webView) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var script=document.createElement('script');" +
                        "var node = document.createTextNode(" + CLICK_LISTENER + ");" +
                        "script.appendChild(node);" +
                        "var body=document.getElementsByTagName(\"body\")[0];" +
                        "body.setAttribute(\"onmousedown\",\"getClick(event)\");" +
                        "var head = document.getElementsByTagName(\"head\")[0];" +
                        "head.appendChild(script);");
            }
        });
    }

    public static void loadPageSource(@NonNull final WebView webView) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var frs=document.getElementsByTagName(\"iframe\");" +
                        "var frameContent=\"\";" +
                        "for(var i=0;i<frs.length;i++){" +
                        "frameContent=frameContent+frs[i].contentDocument.body.parentElement.outerHTML;" +
                        "}" +
                        "window." + JsInteraction.INTERFACE_NAME + ".getPageSource(document.getElementsByTagName('html')[0].innerHTML + frameContent);");
            }
        });
    }

    public static void clickById(@NonNull final WebView webView, final String id) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:document.getElementById(\"" + id + "\").click();");
            }
        });
    }

    public static void clickByName(@NonNull final WebView webView, final String name) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var targets = document.getElementsByName(\"" + name + "\");" +
                        "for(var i = 0; i < targets.length; i++){" +
                        "targets[i].click();" +
                        "}");
            }
        });
    }

    public static void clickByTag(@NonNull final WebView webView, final String tag) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var targets = document.getElementsByTagName(\"" + tag + "\");" +
                        "for(var i = 0; i < targets.length; i++){" +
                        "targets[i].click();" +
                        "}");
            }
        });
    }

    public static void clickByPattern(@NonNull final WebView webView, final String pattern) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var pattern = /" + pattern + "/;" +
                        "var elements = document.all;" +
                        "for(var i=0;i<elements.length;i++){" +
                        "if(pattern.exec(elements[i].outerHTML)){" +
                        "elements[i].click();" +
                        "}" +
                        "}");
            }
        });
    }

    public static void setById(@NonNull final WebView webView, final String id, final String key, final String value) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "document.getElementById(\"" + id + "\").setAttribute(\"" + key + "\",\"" + value + "\");");
            }
        });

    }

    public static void setByTag(@NonNull final WebView webView, final String tag, final String key, final String value) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var targets = document.getElementsByTagName(\"" + tag + "\");" +
                        "for(var i = 0; i < targets.length; i++){" +
                        "targets[i].setAttribute(\"" + key + "\",\"" + value + "\");" +
                        "}");
            }
        });
    }

    public static void setByName(@NonNull final WebView webView, final String name, final String key, final String value) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var targets = document.getElementsByName(\"" + name + "\");" +
                        "for(var i = 0; i < targets.length; i++){" +
                        "targets[i].click();" +
                        "targets[i].setAttribute(\"" + key + "\",\"" + value + "\");" +
                        "}");
            }
        });
    }

    public static void setByPattern(@NonNull final WebView webView, final String pattern, final String key, final String value) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var pattern = /" + pattern + "/;" +
                        "var elements = document.all;" +
                        "for(var i=0;i<elements.length;i++){" +
                        "if(pattern.exec(elements[i].outerHTML)){" +
                        "elements[i].click();" +
                        "elements[i].setAttribute(\"" + key + "\",\"" + value + "\");" +
                        "}" +
                        "}");
            }
        });
    }

    public static void focusById(@NonNull final WebView webView, final String id) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:document.getElementById(\"" + id + "\").focus();");
            }
        });
    }

    public static void focusByName(@NonNull final WebView webView, final String name) {
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("javascript:" +
                        "var targets = document.getElementsByName(\"" + name + "\");" +
                        "for(var i = 0; i < targets.length; i++){" +
                        "targets[i].focus();" +
                        "}");
            }
        });
    }
}
