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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

class FormHandler(document: Document) {

    // use linkedHashMap to ensure form seq
    private val mForms: LinkedHashMap<String, MutableList<Form>> = LinkedHashMap()

    init {
        val elements = document.getElementsByTag("form")
        for (form in elements) {
            addForm(form)
        }
    }

    constructor(html: String, baseURL: String) : this(Jsoup.parse(html, baseURL)) {}

    private fun addForm(form: Element) {
        val name = form.attr("name")
        val stored = mForms[name]
        if (stored == null) {
            val toAdd = ArrayList<Form>()
            toAdd.add(Form(form))
            mForms.put(name, toAdd)
        } else {
            stored.add(Form(form))
        }
    }

    fun getForm(i: Int): Form? {
        var count = -1
        for (forms in mForms.values) {
            for (s in forms) {
                count++
                if (count == i) {
                    return s
                }
            }
        }
        return null
    }
}
