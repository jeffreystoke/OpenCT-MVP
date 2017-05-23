package cc.metapro.openct.data.university;

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

import android.content.Context;
import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.REHelper;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormHandler;

public class UniversityUtils {

    private static final String CLASS_TABLE_PATTERN = "节|(\\d+)";

    static String getLoginFormAction(WebHelper webHelper) {
        Document loginPage = webHelper.getLoginPageDOM();
        FormHandler formHandler = new FormHandler(loginPage);
        Form targetForm = webHelper.getLoginForm() == null ? formHandler.getForm(0) : new Form(webHelper.getLoginForm());
        if (targetForm != null) {
            return targetForm.getAction();
        }
        return "";
    }

    @NonNull
    public static List<Element> getRawClasses(Element table, Context context) {
        if (table == null) return new ArrayList<>();
        if (PrefHelper.getBoolean(context, R.string.pref_class_line_based, false)) {
            List<Element> rawInfoList = new ArrayList<>();
            Elements trs = table.select("tr");
            Elements targetTrs = new Elements();
            if (!trs.isEmpty()) {
                int N = trs.size();
                for (int i = 0; i < N; i++) {
                    if (i >= N) break;

                    Elements innerTables = trs.get(i).getElementsByTag("table");
                    int sum = 0;
                    for (Element innerTable : innerTables) {
                        innerTable.remove();
                        sum += innerTable.select("tr").size();
                        for (Element e : innerTable.select("td")) {
                            trs.get(i).appendChild(e);
                        }
                    }
                    targetTrs.add(trs.get(i));

                    i += sum;
                }

                targetTrs.remove(0);
                for (Element tr : targetTrs) {
                    Elements tds = tr.select("td");
                    StringBuilder builder = new StringBuilder();
                    for (Element td : tds) {
                        builder.append(td.text()).append(HTMLUtils.BR_REPLACER);
                    }
                    Element td = new Element(Tag.valueOf("td"), tr.baseUri());
                    td.text(builder.toString());
                    rawInfoList.add(td);
                }
            }
            return rawInfoList;
        } else {
            String tableString = table.toString();
            tableString = tableString.replaceAll(HTMLUtils.BR, HTMLUtils.BR_REPLACER);
            table = Jsoup.parse(tableString).body().children().first();

            Pattern pattern = Pattern.compile(CLASS_TABLE_PATTERN);
            List<Element> rawInfoList = new ArrayList<>();
            for (Element tr : table.select("tr")) {
                Elements tds = tr.select("th");
                tds.addAll(tr.select("td"));
                Element td = tds.first();
                boolean found = false;
                while (td != null) {
                    if (pattern.matcher(td.text()).find()) {
                        td = td.nextElementSibling();
                        found = true;
                        break;
                    }
                    td = td.nextElementSibling();
                }
                if (!found) {
                    continue;
                }
                int i = 0;
                while (td != null) {
                    i++;
                    rawInfoList.add(td);
                    td = td.nextElementSibling();
                }
                // 补足七天
                for (; i < 7; i++) {
                    rawInfoList.add(new Element(Tag.valueOf("td"), table.baseUri()));
                }
            }
            return rawInfoList;
        }
    }

    @NonNull
    public static Classes generateClasses(Context context, List<Element> rawInfo, ClassTableInfo info) {
        Classes classes = new Classes();
        int[] colors = context.getResources().getIntArray(R.array.class_background);
        if (PrefHelper.getBoolean(context, R.string.pref_class_line_based, false)) {
            for (Element c : rawInfo) {
                if (c.hasText()) {
                    String text = c.text();
                    classes.add(new EnrichedClassInfo(text, DateHelper.chineseToWeekDay(text), 1, info));
                }
            }
        } else {
            int dailyClasses = rawInfo.size() / 7;
            PrefHelper.putString(context, R.string.pref_daily_class_count, dailyClasses + "");
            int colorIndex = 0;
            for (int i = 0; i < 7; i++) {
                for (int j = 0; j < dailyClasses; j++) {
                    Element td = rawInfo.get(j * 7 + i);
                    if (td != null) {
                        String text = td.text();
                        if (!REHelper.isEmpty(text)) {
                            String[] classStrings = text.split(HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+");
                            for (String clazz : classStrings) {
                                colorIndex = colorIndex >= colors.length ? 0 : colorIndex;
                                classes.add(new EnrichedClassInfo(clazz, i + 1, j + 1, colors[colorIndex++], info));
                            }
                        }
                    }
                }
            }
        }
        return classes;
    }

    public static <T> List<T> generateInfoFromTable(Element targetTable, Class<T> tClass) {
        if (targetTable == null) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        Elements trs = targetTable.select("tr");
        if (trs.select("th").isEmpty()) {
            trs.addAll(0, targetTable.select("th"));
        }
        Element th = trs.first();
        trs.remove(0);
        Constructor<T> c;
        try {
            c = tClass.getConstructor(Element.class, Element.class);
            for (Element tr : trs) {
                try {
                    result.add(c.newInstance(th, tr));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return result;
    }
}
