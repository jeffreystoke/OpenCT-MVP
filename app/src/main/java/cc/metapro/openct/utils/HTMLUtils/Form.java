package cc.metapro.openct.utils.HTMLUtils;

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

import com.google.common.base.Strings;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import cc.metapro.openct.utils.Constants;

public class Form {

    private final static Pattern
            formItemPattern = Pattern.compile(Constants.FORM_ITEMS_RE);

    private LinkedHashMap<String, Elements> mFormItems;

    private String mName;
    private String mId;
    private String mMethod;
    private String mAction;

    Form(Element form) {
        mName = form.attr("name");
        mId = form.attr("id");
        mMethod = form.attr("method");
        mAction = form.absUrl("action");

        mFormItems = new LinkedHashMap<>();

        for (Element e : form.getAllElements()) {
            if (formItemPattern.matcher(e.tagName()).find()) {
                if ("select".equalsIgnoreCase(e.tagName())) {
                    Elements options = e.select("option");
                    if (options != null) {
                        Element defaultOption = options.get(0);
                        e = e.attr("value", defaultOption.attr("value"));
                    }
                }
                addFormItem(e);
            }
        }
    }

    public LinkedHashMap<String, Elements> getFormItems() {
        return mFormItems;
    }

    private void addFormItem(Element item) {
        String key = item.attr("name");
        if (Strings.isNullOrEmpty(key)) {
            key = item.attr("id");
        }
        Elements stored = mFormItems.get(key);
        if (stored == null) {
            mFormItems.put(key, new Elements(item));
        } else {
            stored.add(item);
        }
    }

    public String getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getMethod() {
        return mMethod;
    }

    public String getAction() {
        return mAction;
    }

}
