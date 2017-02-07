package cc.metapro.interactiveweb.htmlinterface;

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
import android.webkit.ValueCallback;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface HTMLInspector {

    @NonNull
    String getPageSource();

    /**
     * @return 所有 DOM 对象, 包括 iFrame 中的网页 DOM
     */
    @Nullable
    Document getPageDom();

    @Nullable
    Element getElementById(String id);

    @Nullable
    Elements getElementsByName(String name);

    @Nullable
    Elements getElementsByTag(String tag);

    @Nullable
    Elements getElementsByClass(String c);

    @Nullable
    Elements getElementsByAttr(String attr, @Nullable String value);

}
