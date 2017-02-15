package cc.metapro.openct.data.university.item;

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

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.REHelper;

@Keep
public class GradeInfo {

    private Map<String, String> mTitleValueMap;

    public GradeInfo(Element th, Element tr) {
        mTitleValueMap = new HashMap<>();
        Elements titles = th.select("td");
        if (titles.isEmpty()) {
            titles = th.select("th");
        }
        Elements values = tr.select("td");
        int i = 0;
        for (Element title : titles) {
            String value = values.get(i++).text();
            if (!REHelper.isEmpty(value)) {
                mTitleValueMap.put(title.text(), value);
            }
        }
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        for (String key : mTitleValueMap.keySet()) {
            sb.append(key).append(": ").append(mTitleValueMap.get(key)).append("\n\n");
        }
        if (sb.length() > 2) {
            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.replace(sb.length() - 2, sb.length(), "");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }
}
