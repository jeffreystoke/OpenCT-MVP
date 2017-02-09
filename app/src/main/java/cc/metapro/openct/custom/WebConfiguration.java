package cc.metapro.openct.custom;

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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.LinkedHashMap;


public class WebConfiguration {

    private LinkedHashMap<String, String> mMap;

    public WebConfiguration() {
        mMap = new LinkedHashMap<>();
    }

    public void addAction(Element element, String valueOrFlag) {
        if (element == null) return;
        mMap.remove(element.toString());
        mMap.put(element.toString(), valueOrFlag);
    }

    public LinkedHashMap<Element, String> getClickedCommands() {
        LinkedHashMap<Element, String> map = new LinkedHashMap<>();
        for (String element : mMap.keySet()) {
            map.put(Jsoup.parse(element).body().children().first(), mMap.get(element));
        }
        return map;
    }
}
