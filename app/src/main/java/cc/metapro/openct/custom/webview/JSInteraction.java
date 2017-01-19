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

import android.webkit.JavascriptInterface;

public class JSInteraction {

    public static final String TAG = "HTML";

    public static final String JSInterface = "openct";

    private CallBack mCallBack;

    public JSInteraction(CallBack callBack) {
        mCallBack = callBack;
    }

    @JavascriptInterface
    public void getClicked(String clicked) {
        mCallBack.onClick(clicked);
    }

    public interface CallBack {

        void onClick(String element);

    }
}
