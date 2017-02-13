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

import android.text.TextUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.regex.Pattern;

public class Form {

    public static final String FORM_ITEM_PATTERN = "(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)";

    private LinkedHashMap<String, Elements> mFormItems;

    private String mName;
    private String mId;
    private String mMethod;
    private String mAction;

    public Form() {

    }

    public Form(Element form) {
        mName = form.attr("name");
        mId = form.attr("id");
        mMethod = form.attr("method");
        mAction = form.absUrl("action");

        mFormItems = new LinkedHashMap<>();

        Elements elements = form.getAllElements();
        for (Element e : elements) {
            if (Pattern.compile(FORM_ITEM_PATTERN).matcher(e.tagName()).find()) {
                if ("select".equalsIgnoreCase(e.tagName())) {
                    Elements options = e.select("option");
                    if (options != null) {
                        Element defaultOption = options.get(0);
                        for (Element option : options) {
                            if (option.hasAttr("selected")) {
                                defaultOption = option;
                                break;
                            }
                        }
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
        if (TextUtils.isEmpty(key)) {
            key = item.attr("id");
        }
        Elements stored = mFormItems.get(key);
        if (stored == null) {
            mFormItems.put(key, new Elements(item));
        } else {
            stored.add(item);
        }
    }

    public Element getItemByIndex(int i) {
        int j = 0;
        for (Elements elements : mFormItems.values()) {
            if (j == i) {
                return elements.first();
            }
            j++;
        }
        return null;
    }

    public int size() {
        return mFormItems.size();
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
