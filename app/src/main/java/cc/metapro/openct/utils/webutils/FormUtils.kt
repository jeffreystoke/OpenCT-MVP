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

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import cc.metapro.openct.R
import cc.metapro.openct.utils.Constants
import com.rengwuxian.materialedittext.MaterialEditText
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.util.*
import java.util.regex.Pattern

object FormUtils {

    val INVISIBLE_FORM_ITEM_PATTERN = "(DISPLAY: none)|(hidden)"
    private val TYPE_KEY_PATTERN = "(strSearchType)"
    private val SEARCH_KEY_PATTERN = "(strText)"

    fun getLibSearchQueryMap(form: Form, kvs: Map<String, String>): Map<String, String> {
        val searchType = kvs[Constants.SEARCH_TYPE_KEY]
        val searchContent = kvs[Constants.SEARCH_CONTENT_KEY]
        val res = LinkedHashMap<String, String>()
        var clicked = false

        for (elements in form.formItems.values) {
            val element = classify(elements, null) ?: continue

            var type = element.attr("type")
            var key = element.attr("name")
            var value = element.attr("value")
            if (type.equals("image", ignoreCase = true)) {
                type = "submit"
                key = "x=0&y"
                value = "0"
            }
            val onclick = element.attr("onclick")
            if (Pattern.compile(TYPE_KEY_PATTERN).matcher(key).find()) {
                searchType?.let { res.put(key, it) }
            } else if ("radio".equals(type, ignoreCase = true)) {
                // radio options
                res.put(key, value)
            } else if ("submit".equals(type, ignoreCase = true)) {
                // submit buttons
                if (TextUtils.isEmpty(onclick) && !clicked) {
                    if (!TextUtils.isEmpty(key)) {
                        res.put(key, value)
                    }
                    clicked = true
                }
            } else if ("text".equals(type, ignoreCase = true)) {
                if (Pattern.compile(SEARCH_KEY_PATTERN).matcher(key).find()) {
                    searchContent?.let { res.put(key, it) }
                }
            } else {
                res.put(key, value)
            }
        }
        res.put(Constants.ACTION_KEY, form.action)
        return res
    }

    fun getLoginFiledMap(form: Form, kvs: Map<String, String>, needClick: Boolean): Map<String, String> {
        var prev: Elements? = null
        val loginMap = LinkedHashMap<String, String>()
        var clicked = false
        var passwordOK = false

        for (elements in form.formItems.values) {
            val element = classify(elements, null) ?: continue
            val type = element.attr("type")
            val key = element.attr("name")
            val value = element.attr("value")
            val onclick = element.attr("onclick")
            val id = element.id()
            if ("radio".equals(type, ignoreCase = true)) {
                loginMap.put(key, value)
            } else if ("submit".equals(type, ignoreCase = true)) {
                // submit buttons
                if (TextUtils.isEmpty(onclick) && !clicked) {
                    if (needClick) {
                        loginMap.put(key, value)
                    }
                    clicked = true
                }
            } else if ("password".equals(type, ignoreCase = true) && prev != null) {
                // password text
                val username = kvs[Constants.USERNAME_KEY]
                val password = kvs[Constants.PASSWORD_KEY]

                // 填写用户名
                var userNameKey = prev.attr("name")
                if (TextUtils.isEmpty(userNameKey)) {
                    userNameKey = prev.attr("id")
                }
                username?.let { loginMap.put(userNameKey, it) }

                // 填写密码
                if (!TextUtils.isEmpty(key)) {
                    password?.let { loginMap.put(key, it) }
                } else {
                    password?.let { loginMap.put(id, it) }
                }
                passwordOK = true
            } else if ("text".equals(type, ignoreCase = true)) {
                // common text
                // secret code text (after password)
                if (prev != null && passwordOK) {
                    val code = kvs[Constants.CAPTCHA_KEY]
                    if (!TextUtils.isEmpty(key)) {
                        code?.let { loginMap.put(key, it) }
                    } else {
                        code?.let { loginMap.put(id, it) }
                    }
                    passwordOK = false
                } else {
                    if (Pattern.compile(INVISIBLE_FORM_ITEM_PATTERN).matcher(elements.toString()).find()) {
                        loginMap.put(key, value)
                    }
                }
            } else {
                loginMap.put(key, value)
            }
            prev = elements
        }
        loginMap.put(Constants.ACTION_KEY, form.action)
        return loginMap
    }

    private fun classify(elements: Elements, preferedValue: String?): Element? {
        if (elements.size == 0) return null
        val tagName = elements[0].tagName()
        if ("input".equals(tagName, ignoreCase = true)) {
            val type = elements.attr("type")
            when (type) {
                "radio" -> return radio(elements, preferedValue)
            }
        }
        return elements[0]
    }

    private fun radio(radios: Elements, preferedValue: String?): Element {
        radios
                .filter { it.hasAttr("checked") }
                .forEach {
                    if (!TextUtils.isEmpty(preferedValue)) {
                        return it.attr("value", preferedValue)
                    }
                    return it
                }
        return radios[0]
    }

    @Throws(Exception::class)
    fun getFormView(context: Context, container: ViewGroup?, form: Form?): View {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_empty_vertical_linearlayout, container, false)
        val baseLinearLayout = view.findViewById<View>(R.id.content) as LinearLayout
        val formItems = form!!.formItems
        for (elements in formItems.values) {
            val e = elements.first()
            val tagName = e.tagName()
            if ("select".equals(tagName, ignoreCase = true)) {
                val options = e.select("option")
                val texts = ArrayList<String>()
                for (opt in options) {
                    texts.add(opt.text())
                }
                val spinner = Spinner(context)
                spinner.adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, texts)
                spinner.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                baseLinearLayout.addView(spinner)
            }
            if ("input".equals(tagName, ignoreCase = true)) {
                if ("text".equals(e.attr("type"), ignoreCase = true)) {
                    val editText = MaterialEditText(context)
                    editText.layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    baseLinearLayout.addView(editText)
                }
            }
        }
        return view
    }
}
