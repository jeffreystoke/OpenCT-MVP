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

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import cc.metapro.interactiveweb.utils.JSUtils;


public class InteractiveWebViewClient extends WebViewClient {

    private String CURRENT_URL;
    private FinishCallBack mFinishCallBack;
    private StartCallBack mStartCallBack;

    String getCurrentPageURL() {
        return CURRENT_URL;
    }

    void setOnStartCallBack(StartCallBack startCallBack) {
        mStartCallBack = startCallBack;
    }

    void removeOnStartCallBack() {
        mStartCallBack = null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        if (mStartCallBack != null) {
            mStartCallBack.onPageStart();
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        if (mStartCallBack != null) {
            mStartCallBack.onPageStart();
        }
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (mStartCallBack != null) {
            mStartCallBack.onPageStart();
        }
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        handler.proceed();
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        JSUtils.injectClickListener(view);
        JSUtils.loadPageSource(view);
        if (mStartCallBack != null) {
            mStartCallBack.onPageStart();
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        CURRENT_URL = url;
        JSUtils.injectClickListener(view);
        JSUtils.loadPageSource(view);
        if (mFinishCallBack != null) {
            mFinishCallBack.onPageFinish();
        }
        super.onPageFinished(view, url);
    }

    public interface StartCallBack {
        void onPageStart();
    }

    public interface FinishCallBack {
        void onPageFinish();
    }
}
