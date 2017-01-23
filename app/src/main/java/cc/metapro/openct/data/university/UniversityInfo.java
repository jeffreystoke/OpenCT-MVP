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

import android.support.annotation.Keep;

import com.google.gson.Gson;

import java.util.Map;

import cc.metapro.openct.data.source.DBHelper;
import cc.metapro.openct.data.source.StoreHelper;

@Keep
public class UniversityInfo {

    public CMSInfo mCMSInfo;
    public LibraryInfo mLibraryInfo;

    public static class LibraryInfo {

        public String mLibSys, mLibURL;

        public boolean mNeedCAPTCHA;

        public LibraryFactory.BorrowTableInfo mBorrowTableInfo;

        @Override
        public String toString() {
            return StoreHelper.getJsonText(this);
        }
    }

    public static class CMSInfo {

        public boolean mNeedCAPTCHA, mDynLoginURL;

        public String mCmsSys, mCmsURL;

        public CmsFactory.ClassTableInfo mClassTableInfo;
        public CmsFactory.GradeTableInfo mGradeTableInfo;

        @Override
        public String toString() {
            return StoreHelper.getJsonText(this);
        }
    }

    public static class SchoolInfo {

        public String name, cmsSys, cmsURL, libSys, libURL;

        public boolean
                cmsDynURL, cmsCaptcha, cmsInnerAccess,
                libDynURL, libCaptcha, libInnerAccess;

        public SchoolInfo() {

        }

        public SchoolInfo(Map<String, String> stringMap, Map<String, Boolean> booleanMap) {
            try {
                name = stringMap.get(DBHelper.SCHOOL_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                cmsSys = stringMap.get(DBHelper.CMS_SYS);
                cmsURL = stringMap.get(DBHelper.CMS_URL);
                cmsDynURL = booleanMap.get(DBHelper.CMS_DYN_URL);
                cmsCaptcha = booleanMap.get(DBHelper.CMS_CAPTCHA);
                cmsInnerAccess = booleanMap.get(DBHelper.CMS_INNER_ACCESS);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                libSys = stringMap.get(DBHelper.LIB_SYS);
                libURL = stringMap.get(DBHelper.LIB_URL);
                libDynURL = booleanMap.get(DBHelper.LIB_DYN_URL);
                libCaptcha = booleanMap.get(DBHelper.LIB_CAPTCHA);
                libInnerAccess = booleanMap.get(DBHelper.LIB_INNER_ACCESS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String toString() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }
}