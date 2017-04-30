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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.utils.Constants;

public class DetailCustomInfo {

    public ClassTableInfo mClassTableInfo;
    private String mSchoolName = Constants.DEFAULT_SCHOOL_NAME;

    @NonNull
    private String GRADE_TABLE_ID = "";
    @NonNull
    private String BORROW_TABLE_ID = "";
    @NonNull
    private List<String> mClassUrlPatterns = new LinkedList<>();
    @NonNull
    private List<String> mGradeUrlPatterns = new LinkedList<>();
    @NonNull
    private List<String> mBorrowUrlPatterns = new LinkedList<>();

    public DetailCustomInfo() {

    }

    public DetailCustomInfo(String schoolName) {
        mSchoolName = schoolName;
    }

    @NonNull
    public String getGradeTableId() {
        return GRADE_TABLE_ID;
    }

    public void setGradeTableId(@NonNull String GRADE_TABLE_ID) {
        this.GRADE_TABLE_ID = GRADE_TABLE_ID;
    }

    @NonNull
    public String getBorrowTableId() {
        return BORROW_TABLE_ID;
    }

    public void setBorrowTableId(@NonNull String BORROW_TABLE_ID) {
        this.BORROW_TABLE_ID = BORROW_TABLE_ID;
    }

    @NonNull
    public List<String> getClassUrlPatterns() {
        return mClassUrlPatterns;
    }

    @NonNull
    public List<String> getGradeUrlPatterns() {
        return mGradeUrlPatterns;
    }

    @NonNull
    public List<String> getBorrowUrlPatterns() {
        return mBorrowUrlPatterns;
    }

    public String getSchoolName() {
        return mSchoolName;
    }

    public void setFirstClassUrlPattern(String urlPattern) {
        mClassUrlPatterns = new ArrayList<>();
        mClassUrlPatterns.add(urlPattern);
    }

    public void setFirstGradeUrlPattern(String urlPattern) {
        mGradeUrlPatterns = new ArrayList<>();
        mGradeUrlPatterns.add(urlPattern);
    }

    public void setFirstBorrowPattern(String urlPattern) {
        mBorrowUrlPatterns = new ArrayList<>();
        mBorrowUrlPatterns.add(urlPattern);
    }

    public void addClassUrlPattern(String urlPattern) {
        mClassUrlPatterns.add(urlPattern);
    }

    public void addGradeUrlPattern(String urlPattern) {
        mGradeUrlPatterns.add(urlPattern);
    }

    public void addBorrowPattern(String urlPattern) {
        mBorrowUrlPatterns.add(urlPattern);
    }

    public void setClassTableInfo(ClassTableInfo classTableInfo) {
        mClassTableInfo = classTableInfo;
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }
}
