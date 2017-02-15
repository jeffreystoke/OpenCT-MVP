package cc.metapro.interactiveweb.utils;

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

import android.support.annotation.Nullable;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class HTMLUtils {

    public static final String BR = "(<\\s*?br\\s*?/?>)|(---+)|(◇)";
    public static final String BR_REPLACER = "&";

    public static boolean isUserInput(Element element) {
        return isTextInput(element) || isPasswordInput(element);
    }

    public static boolean isTextInput(Element element) {
        return isInput(element) && "text".equalsIgnoreCase(element.attr("type"));
    }

    public static boolean isPasswordInput(Element element) {
        return isInput(element) && "password".equalsIgnoreCase(element.attr("type"));
    }

    public static boolean isInput(Element element) {
        return element != null && "input".equalsIgnoreCase(element.tagName());
    }

    public static boolean isClickable(Element element) {
        return element != null &&
                ("button".equalsIgnoreCase(element.tagName())
                        || "submit".equalsIgnoreCase(element.attr("type"))
                        || "a".equalsIgnoreCase(element.tagName()));
    }

    public static String getElementPattern(Element element) {
        return element.toString();
    }

    @Nullable
    public static Element getElementSimilar(Document document, Element element) {
        if (element == null) return null;
        Elements elements = document.select(element.tagName());
        Element result = null;
        boolean found = true;
        for (Element e : elements) {
            result = e;
            for (Attribute attribute : element.attributes()) {
                if (!attribute.getValue().equals(e.attr(attribute.getKey()))) {
                    found = false;
                    break;
                }
            }
            if (found) {
                return result;
            } else {
                found = true;
            }
        }
        return null;
    }
}
