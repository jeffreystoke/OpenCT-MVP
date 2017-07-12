package cc.metapro.openct.utils.webutils

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

import android.text.TextUtils
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*
import java.util.regex.Pattern

class Form {

    val formItems: LinkedHashMap<String, Elements>

    val name: String
    val id: String
    val method: String
    val action: String

    constructor() {

    }

    constructor(form: Element) {
        name = form.attr("name")
        id = form.attr("id")
        method = form.attr("method")
        action = form.absUrl("action")

        formItems = LinkedHashMap<String, Elements>()

        val elements = form.allElements
        for (e in elements) {
            if (Pattern.compile(FORM_ITEM_PATTERN).matcher(e.tagName()).find()) {
                if ("select".equals(e.tagName(), ignoreCase = true)) {
                    val options = e.select("option")
                    if (options != null) {
                        var defaultOption = options[0]
                        for (option in options) {
                            if (option.hasAttr("selected")) {
                                defaultOption = option
                                break
                            }
                        }
                        e = e.attr("value", defaultOption.attr("value"))
                    }
                }
                addFormItem(e)
            }
        }
    }

    private fun addFormItem(item: Element) {
        var key = item.attr("name")
        if (TextUtils.isEmpty(key)) {
            key = item.attr("id")
        }
        val stored = formItems[key]
        stored?.add(item) ?: formItems.put(key, Elements(item))
    }

    fun getItemByIndex(i: Int): Element? {
        var j = 0
        for (elements in formItems.values) {
            if (j == i) {
                return elements.first()
            }
            j++
        }
        return null
    }

    fun size(): Int {
        return formItems.size
    }

    companion object {

        val FORM_ITEM_PATTERN = "(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)"
    }

}
