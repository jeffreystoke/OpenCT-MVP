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
import android.support.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.REHelper;

@Keep
public class BorrowInfo {

    @NonNull
    private Map<String, String> mTitleValueMap = new LinkedHashMap<>();

    public BorrowInfo(Element th, Element tr) {
        mTitleValueMap = new LinkedHashMap<>();
        Elements titles = th.select("td");
        Elements values = tr.select("td");
        int i = 0;
        for (Element title : titles) {
            String value = values.get(i++).text();
            if (!REHelper.isEmpty(value)) {
                mTitleValueMap.put(title.text(), value);
            }
        }
    }

    public boolean isExceeded(Date toDay) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date date = format.parse(mTitleValueMap.get("应还日期"));
            return date.before(toDay);
        } catch (Exception ignored) {

        }
        return false;
    }

    public String getFilteredContent(List<String> filter) {
        if (filter != null) {
            Collections.sort(filter);
        }

        StringBuilder sb = new StringBuilder();
        if (!mTitleValueMap.isEmpty()) {
            for (String key : mTitleValueMap.keySet()) {
                if (filter != null) {
                    int i = Collections.binarySearch(filter, key);
                    if (i >= 0) {
                        sb.append(key).append(": ").append(mTitleValueMap.get(key)).append("\n\n");
                    }
                } else {
                    sb.append(key).append(": ").append(mTitleValueMap.get(key)).append("\n\n");
                }
            }

            if (sb.charAt(sb.length() - 1) == '\n') {
                sb.replace(sb.length() - 2, sb.length(), "");
            }
        }
        return sb.toString();
    }

    public Collection<String> getTitles() {
        return mTitleValueMap.keySet();
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }

}
