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

import android.webkit.JavascriptInterface;

import java.util.ArrayList;
import java.util.List;

public class JsInteraction {

    public static final String INTERFACE_NAME = "WebInteractionInterface";
    static final int ON_CLICK = 3;
    static final int ON_LOAD_SOURCE = 4;
    private List<CallBack> mCallBackList = new ArrayList<>();

    JsInteraction(CallBack callBack) {
        mCallBackList.add(callBack);
    }

    @JavascriptInterface
    public void getPageSource(String html) {
        int N = mCallBackList.size();
        for (int i = 0; i < N; i++) {
            mCallBackList.get(i).onAction(ON_LOAD_SOURCE, html);
        }
    }

    @JavascriptInterface
    public void onClicked(String element) {
        int N = mCallBackList.size();
        for (int i = 0; i < N; i++) {
            mCallBackList.get(i).onAction(ON_CLICK, element);
        }
    }

    interface CallBack {
        void onAction(int type, String domItem);
    }

}
