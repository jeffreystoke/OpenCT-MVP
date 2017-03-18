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

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public interface HTMLInspector {

    String getPageSource();

    /**
     * @return 所有 DOM 对象, 包括 iFrame 中的网页 DOM
     */
    Document getPageDom();

    Element getElementById(String id);

    Elements getElementsByName(String name);

    Elements getElementsByTag(String tag);

    Elements getElementsByClass(String c);

    Elements getElementsByAttr(String attr, String value);

}
