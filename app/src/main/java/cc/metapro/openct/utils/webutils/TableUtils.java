package cc.metapro.openct.utils.webutils;

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
import android.text.TextUtils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.Map;

public class TableUtils {

    @NonNull
    public static Map<String, Element> getTablesFromTargetPage(Document html) {
        if (html == null) return new HashMap<>();
        Map<String, Element> result = new HashMap<>();
        Elements tables = html.select("table");
        int i = 0;
        for (Element table : tables) {
            String id = table.id();
            if (TextUtils.isEmpty(id)) {
                id = table.attr("name");
            }

            if (TextUtils.isEmpty(id)) {
                id = table.attr("class");
            }

            if (TextUtils.isEmpty(id)) {
                id = i + "";
            }

            if (result.get(id) == null) {
                result.put(id, table);
            }
            i++;
        }
        return result;
    }
}
