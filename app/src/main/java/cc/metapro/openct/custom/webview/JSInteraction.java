package cc.metapro.openct.custom.webview;

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
import android.webkit.JavascriptInterface;

@Keep
public class JSInteraction {

    public static final String TAG = "HTML";

    public static final String JSInterface = "openct";

    private ClickCallBack mClickCallBack;

    private RawCallBack mRawCallBack;

    public JSInteraction(ClickCallBack clickCallBack, RawCallBack rawCallBack) {
        mClickCallBack = clickCallBack;
        mRawCallBack = rawCallBack;
    }

    @JavascriptInterface
    public void getRaw(String html) {
        mRawCallBack.onLoadRaw(html);
    }

    @JavascriptInterface
    public void getClicked(String id) {
        mClickCallBack.onClick(id);
    }

    public interface ClickCallBack {

        void onClick(String id);

    }

    public interface RawCallBack {

        void onLoadRaw(String html);

    }
}
