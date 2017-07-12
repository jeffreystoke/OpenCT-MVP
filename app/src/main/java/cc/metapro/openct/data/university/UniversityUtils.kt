package cc.metapro.openct.data.university

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
import cc.metapro.interactiveweb.utils.HTMLUtils
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo
import cc.metapro.openct.utils.DateHelper
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.REHelper
import cc.metapro.openct.utils.webutils.Form
import cc.metapro.openct.utils.webutils.FormHandler
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.jsoup.parser.Tag
import org.jsoup.select.Elements
import java.lang.reflect.Constructor
import java.util.*
import java.util.regex.Pattern

object UniversityUtils {

    private val CLASS_TABLE_PATTERN = "节|(\\d+)"

    internal fun getLoginFormAction(webHelper: WebHelper): String {
        val loginPage = webHelper.loginPageDOM
        val formHandler = FormHandler(loginPage!!)
        val targetForm = if (webHelper.loginForm == null) formHandler.getForm(0) else Form(webHelper.loginForm!!)
        if (targetForm != null) {
            return targetForm.action
        }
        return ""
    }

    fun getRawClasses(table: Element?, context: Context): List<Element> {
        var table: Element? = table ?: return ArrayList()
        if (PrefHelper.getBoolean(context, R.string.pref_class_line_based, false)) {
            val rawInfoList = ArrayList<Element>()
            val trs = table!!.select("tr")
            val targetTrs = Elements()
            if (!trs.isEmpty()) {
                val N = trs.size
                var i = 0
                while (i < N) {
                    if (i >= N) break

                    val innerTables = trs[i].getElementsByTag("table")
                    var sum = 0
                    for (innerTable in innerTables) {
                        innerTable.remove()
                        sum += innerTable.select("tr").size
                        for (e in innerTable.select("td")) {
                            trs[i].appendChild(e)
                        }
                    }
                    targetTrs.add(trs[i])

                    i += sum
                    i++
                }

                targetTrs.removeAt(0)
                for (tr in targetTrs) {
                    val tds = tr.select("td")
                    val builder = StringBuilder()
                    for (td in tds) {
                        builder.append(td.text()).append(HTMLUtils.BR_REPLACER)
                    }
                    val td = Element(Tag.valueOf("td"), tr.baseUri())
                    td.text(builder.toString())
                    rawInfoList.add(td)
                }
            }
            return rawInfoList
        } else {
            var tableString = table.toString()
            tableString = tableString.replace(HTMLUtils.BR.toRegex(), HTMLUtils.BR_REPLACER)
            table = Jsoup.parse(tableString).body().children().first()

            val pattern = Pattern.compile(CLASS_TABLE_PATTERN)
            val rawInfoList = ArrayList<Element>()
            for (tr in table!!.select("tr")) {
                val tds = tr.select("th")
                tds.addAll(tr.select("td"))
                var td: Element? = tds.first()
                var found = false
                while (td != null) {
                    if (pattern.matcher(td.text()).find()) {
                        td = td.nextElementSibling()
                        found = true
                        break
                    }
                    td = td.nextElementSibling()
                }
                if (!found) {
                    continue
                }
                var i = 0
                while (td != null) {
                    i++
                    rawInfoList.add(td)
                    td = td.nextElementSibling()
                }
                // 补足七天
                while (i < 7) {
                    rawInfoList.add(Element(Tag.valueOf("td"), table.baseUri()))
                    i++
                }
            }
            return rawInfoList
        }
    }

    fun generateClasses(context: Context, rawInfo: List<Element>, info: ClassTableInfo): Classes {
        val classes = Classes()
        val colors = context.resources.getIntArray(R.array.class_background)
        if (PrefHelper.getBoolean(context, R.string.pref_class_line_based, false)) {
            for (c in rawInfo) {
                if (c.hasText()) {
                    val text = c.text()
                    classes.add(EnrichedClassInfo(text, DateHelper.chineseToWeekDay(text), 1, info))
                }
            }
        } else {
            val dailyClasses = rawInfo.size / 7
            PrefHelper.putString(context, R.string.pref_daily_class_count, dailyClasses.toString() + "")
            var colorIndex = 0
            for (i in 0..6) {
                for (j in 0..dailyClasses - 1) {
                    val td = rawInfo[j * 7 + i]
                    if (td != null) {
                        val text = td.text()
                        if (!REHelper.isEmpty(text)) {
                            val classStrings = text.split((HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                            for (clazz in classStrings) {
                                colorIndex = if (colorIndex >= colors.size) 0 else colorIndex
                                classes.add(EnrichedClassInfo(clazz, i + 1, j + 1, colors[colorIndex++], info))
                            }
                        }
                    }
                }
            }
        }
        return classes
    }

    fun <T> generateInfoFromTable(targetTable: Element?, tClass: Class<T>): List<T> {
        if (targetTable == null) return ArrayList()
        val result = ArrayList<T>()
        val trs = targetTable.select("tr")
        if (trs.select("th").isEmpty()) {
            trs.addAll(0, targetTable.select("th"))
        }
        val th = trs.first()
        trs.removeAt(0)
        val c: Constructor<T>
        try {
            c = tClass.getConstructor(Element::class.java, Element::class.java)
            for (tr in trs) {
                try {
                    result.add(c.newInstance(th, tr))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        }

        return result
    }
}
