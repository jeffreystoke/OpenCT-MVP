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

import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;
import java.util.Set;

import cc.metapro.interactiveweb.utils.JSONHelper;

public class WebConfiguration {

    private LinkedHashMap<Element, String> mClickValueMap;

    public WebConfiguration() {
        mClickValueMap = new LinkedHashMap<>();
    }

    public void addAction(Element element, String valueOrFlag) {
        mClickValueMap.remove(element);
        mClickValueMap.put(element, valueOrFlag);
    }

    public Set<Element> getAllElements() {
        return mClickValueMap.keySet();
    }

    @Override
    public String toString() {
        return JSONHelper.toJson(this);
    }
}
