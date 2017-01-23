package cc.metapro.openct.data.university.item;

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

import android.support.annotation.Keep;

import org.jsoup.select.Elements;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.REHelper;

@Keep
public class GradeInfo {
    private String
            mClassCode, mClassName, mClassType, mPoints,
            mGradeSummary, mGradePractice, mGradeCommon,
            mGradeMidExam, mGradeFinalExam, mGradeMakeup;

    public GradeInfo(Elements tds, CmsFactory.GradeTableInfo gradeInfo) {
        try {
            mClassCode = tds.get(gradeInfo.mClassCodeIndex).text();
            mClassName = tds.get(gradeInfo.mClassNameIndex).text();
            mClassType = tds.get(gradeInfo.mClassTypeIndex).text();
            mPoints = tds.get(gradeInfo.mPointsIndex).text();
            mGradeSummary = tds.get(gradeInfo.mGradeSummaryIndex).text();
            mGradePractice = tds.get(gradeInfo.mGradePracticeIndex).text();
            mGradeCommon = tds.get(gradeInfo.mGradeCommonIndex).text();
            mGradeMidExam = tds.get(gradeInfo.mGradeMidExamIndex).text();
            mGradeFinalExam = tds.get(gradeInfo.mGradeFinalExamIndex).text();
            mGradeMakeup = tds.get(gradeInfo.mGradeMakeupIndex).text();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        if (!REHelper.isEmpty(mClassName)) sb.append("名称: ").append(mClassName).append("\n\n");
        if (!REHelper.isEmpty(mClassType)) sb.append("类型: ").append(mClassType).append("\n\n");
        if (!REHelper.isEmpty(mPoints)) sb.append("学分: ").append(mPoints).append("\n\n");
        if (!REHelper.isEmpty(mGradeSummary))
            sb.append("成绩: ").append(mGradeSummary).append("\n\n");
        if (!REHelper.isEmpty(mGradeCommon))
            sb.append("平时成绩: ").append(mGradeCommon).append("\n\n");
        if (!REHelper.isEmpty(mGradePractice))
            sb.append("实践成绩: ").append(mGradePractice).append("\n\n");
        if (!REHelper.isEmpty(mGradeMidExam))
            sb.append("期中成绩: ").append(mGradeMidExam).append("\n\n");
        if (!REHelper.isEmpty(mGradeFinalExam))
            sb.append("期末成绩: ").append(mGradeFinalExam).append("\n\n");
        if (!REHelper.isEmpty(mGradeMakeup))
            sb.append("补考成绩: ").append(mGradeMakeup).append("\n\n");

        if (sb.charAt(sb.length() - 1) == '\n') {
            sb.replace(sb.length() - 2, sb.length(), "");
        }
        return sb.toString();
    }

    public String getClassName() {
        return mClassName;
    }

    public String getGradeSummary() {
        return mGradeSummary;
    }
}
