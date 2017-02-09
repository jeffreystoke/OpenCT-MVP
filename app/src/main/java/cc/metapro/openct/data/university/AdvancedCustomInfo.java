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
import android.support.annotation.Keep;

import cc.metapro.openct.R;
import cc.metapro.openct.custom.WebConfiguration;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.PrefHelper;

@Keep
public class AdvancedCustomInfo {

    public CmsFactory.ClassTableInfo mClassTableInfo;

    public WebConfiguration mClassWebConfig;
    public WebConfiguration mGradeWebConfig;
    public WebConfiguration mBorrowWebConfig;
//    public WebConfiguration mSearchWebConfig;

    public String CLASS_URL_PATTERN;
    public String GRADE_URL_PATTERN;
    public String BORROW_URL_PATTERN;

    public String GRADE_TABLE_ID;
    public String BORROW_TABLE_ID;

    private String mSchoolName;

    public AdvancedCustomInfo(Context context) {
        if (PrefHelper.getBoolean(context, R.string.pref_custom_enable)) {
            mSchoolName = PrefHelper.getString(context, R.string.pref_school_name);
        } else {
            mSchoolName = PrefHelper.getString(context, R.string.pref_school_name);
        }
    }

    public String getSchoolName() {
        return mSchoolName;
    }

    public void setClassTableInfo(CmsFactory.ClassTableInfo classTableInfo) {
        mClassTableInfo = classTableInfo;
    }

    public void setClassWebConfig(WebConfiguration classWebConfig) {
        mClassWebConfig = classWebConfig;
    }

    public void setGradeWebConfig(WebConfiguration gradeWebConfig) {
        mGradeWebConfig = gradeWebConfig;
    }

    public void setBorrowWebConfig(WebConfiguration borrowWebConfig) {
        mBorrowWebConfig = borrowWebConfig;
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }
}
