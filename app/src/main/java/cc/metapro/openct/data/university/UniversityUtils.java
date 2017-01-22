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

import android.support.annotation.NonNull;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.Constants;

public class UniversityUtils {

    private static final String CLASS_TABLE_PATTERN = "(\\d+.*\\d+节$)|(\\d+节$)";

    /**
     * 从网页课表表格中获取所有课程信息单元 (td - 按格显示课程, tr - 按行显示课程)
     *
     * @param table 网页原始表格
     * @return 课程信息的原始状态
     */
    @NonNull
    public static List<Element>
    getRawClasses(Element table) {
        // TODO: 17/1/22 判断课表信息是否在TD中, 并将TR中的课程信息转换到TD中
        Pattern pattern = Pattern.compile(CLASS_TABLE_PATTERN);
        List<Element> tdWithClassInfo = new ArrayList<>();
        for (Element tr : table.select("tr")) {
            Elements tds = tr.select("td");
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
                tdWithClassInfo.add(td);
                td = td.nextElementSibling();
            }
            // 补足七天
            for (; i < 7; i++) {
                tdWithClassInfo.add(new Element(Tag.valueOf("td"), table.baseUri()));
            }
        }
        return tdWithClassInfo;
    }

    @NonNull
    public static List<EnrichedClassInfo>
    generateClasses(List<Element> rawInfo, CmsFactory.ClassTableInfo info) {
        List<ClassInfo> classes = new ArrayList<>(rawInfo.size());
        for (Element td : rawInfo) {
            classes.add(new ClassInfo(td.text(), info));
        }
        List<EnrichedClassInfo> enrichedClasses = new ArrayList<>(classes.size());
        int dailyClasses = classes.size() / 7;

        for (int i = 0; i < 7; i++) {
            int colorIndex = i;
            if (colorIndex > Constants.colorString.length) {
                colorIndex /= 3;
            }
            for (int j = 0; j < dailyClasses; j++) {
                colorIndex++;
                if (colorIndex >= Constants.colorString.length) {
                    colorIndex = 0;
                }
                ClassInfo classInfo = classes.get(j * 7 + i);
                if (classInfo == null) {
                    continue;
                }

                if (!classInfo.isEmpty()) {
                    enrichedClasses.add(new EnrichedClassInfo(classInfo, i + 1, j + 1, Constants.getColor(colorIndex)));
                }
            }
        }
        return enrichedClasses;
    }

    @NonNull
    public static List<GradeInfo>
    generateGrades(String classTablePage, CmsFactory.GradeTableInfo gradeTableInfo) {
        Document doc = Jsoup.parse(classTablePage);
        Elements tables = doc.select("table[id=" + gradeTableInfo.mGradeTableID + "]");
        Element targetTable = tables.first();

        // 不是标准Id, 使用表头匹配
        if (targetTable == null) {
            tables = doc.select("table:matches(绩)");
            targetTable = tables.first();
        }

        if (targetTable == null) {
            return new ArrayList<>(0);
        }

        List<GradeInfo> grades = new ArrayList<>();
        Elements trs = targetTable.select("tr");
        trs.remove(0);
        for (Element tr : trs) {
            Elements tds = tr.select("td");
            grades.add(new GradeInfo(tds, gradeTableInfo));
        }
        return grades;
    }
}
