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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;

import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

@Keep
public class UniversityUtils {

    private static final String CLASS_TABLE_PATTERN = "节|(\\d+)";

    @NonNull
    public static List<Element> getRawClasses(Element table, Context context) {
        if (table == null) return new ArrayList<>();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean(context.getString(R.string.pref_class_line_based), false)) {
            List<Element> rawInfoList = new ArrayList<>();
            Elements trs = table.select("th");
            trs.addAll(table.select("tr"));
            if (!trs.isEmpty()) {
                trs.remove(0);
                for (Element tr : trs) {
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
    public static List<EnrichedClassInfo> generateClasses(Context context, List<Element> rawInfo, CmsFactory.ClassTableInfo info) {
        List<ClassInfo> classes = new ArrayList<>(rawInfo.size());
        List<EnrichedClassInfo> enrichedClasses = new ArrayList<>(classes.size());
        int[] colors = context.getResources().getIntArray(R.array.class_background);
        if (PrefHelper.getBoolean(context, R.string.pref_class_line_based)) {
            for (Element td : rawInfo) {
                classes.add(new ClassInfo(td.text(), info));
            }
            for (ClassInfo c : classes) {
                if (!c.isEmpty()) {
                    enrichedClasses.add(new EnrichedClassInfo(c));
                }
            }
        } else {
            for (Element td : rawInfo) {
                classes.add(new ClassInfo(td.text(), info));
            }
            int dailyClasses = classes.size() / 7;

            for (int i = 0; i < 7; i++) {
                int colorIndex = i;
                if (colorIndex > colors.length) {
                    colorIndex /= 3;
                }
                for (int j = 0; j < dailyClasses; j++) {
                    colorIndex++;
                    if (colorIndex >= colors.length) {
                        colorIndex = 0;
                    }
                    ClassInfo classInfo = classes.get(j * 7 + i);
                    if (classInfo == null) {
                        continue;
                    }

                    if (!classInfo.isEmpty()) {
                        enrichedClasses.add(new EnrichedClassInfo(classInfo, i + 1, j + 1, colors[colorIndex]));
                    }
                }
            }
        }
        return enrichedClasses;
    }

    public static <T> List<T> generateInfo(Element targetTable, Class<T> tClass) {
        if (targetTable == null) return new ArrayList<>();
        List<T> result = new ArrayList<>();
        Elements trs = targetTable.select("th");
        trs.addAll(targetTable.select("tr"));
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

    @NonNull
    static String QZEncryption(String serverResponse, Map<String, String> loginMap) {
        String scode = serverResponse.split("#")[0];
        String sxh = serverResponse.split("#")[1];
        String code = loginMap.get(Constants.USERNAME_KEY) + "%%%" + loginMap.get(Constants.PASSWORD_KEY);
        String encoded = "";
        for (int i = 0; i < code.length(); i++) {
            if (i < 20) {
                encoded = encoded + code.substring(i, i + 1) + scode.substring(0, Integer.parseInt(sxh.substring(i, i + 1)));
                scode = scode.substring(Integer.parseInt(sxh.substring(i, i + 1)), scode.length());
            } else {
                encoded = encoded + code.substring(i, code.length());
                i = code.length();
            }
        }

        return encoded;
    }

    static String MD5Encryption(Map<String, String> loginMap) {
        loginMap.get(Constants.PASSWORD_KEY);
        return null;
    }
}
