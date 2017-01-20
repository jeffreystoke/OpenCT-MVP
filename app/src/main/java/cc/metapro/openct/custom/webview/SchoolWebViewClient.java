package cc.metapro.openct.custom.webview;

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

import android.net.http.SslError;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SchoolWebViewClient extends WebViewClient {

    // 注入到加载的HTML文件中, 监听点击事件
    private static final String CLICK_LISTENER =
            "\"function getClicked(e){" +
                    "var targ;if(!e){var e=window.event;}" +
                    "if(e.target){targ=e.target;}" +
                    "else if(e.srcElement){targ=e.srcElement;}" +
                    "if(targ.nodeType==3){targ=targ.parentNode;}" +
                    "var id;id=targ.id;if(id){id=targ.name;}" +
                    "if(targ.href){id=targ.href;}" +
                    "window." + JSInteraction.JSInterface + ".getClicked(id);}\"";
    public static boolean replayMode = false;
    private Observer<Integer> mObserver;
    private int actionIndex;

    public void setActions(final List<String> actions, final WebView webView) {
        actionIndex = 0;
        mObserver = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                webView.loadUrl(actions.get(integer));
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (!replayMode) {
            // 注入监听JS脚本
            view.loadUrl("javascript:var script = document.createElement('script');" +
                    "var node = document.createTextNode(" + CLICK_LISTENER + ");" +
                    "script.appendChild(node);" +
                    "var body=document.getElementsByTagName(\"body\").item(0);" +
                    "body.setAttribute(\"onmousedown\",\"getClicked(event)\");" +
                    "var head = document.getElementsByTagName(\"head\").item(0);" +
                    "head.appendChild(script);");
        } else {
            if (mObserver != null) {
                mObserver.onNext(actionIndex++);
            }
        }
        super.onPageFinished(view, url);
    }
}
