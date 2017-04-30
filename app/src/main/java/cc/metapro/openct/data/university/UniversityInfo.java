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

import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.utils.CharacterParser;
import cc.metapro.openct.utils.Constants;

public class UniversityInfo implements Comparable<UniversityInfo> {

    private String name = Constants.DEFAULT_SCHOOL_NAME;
    private String cmsSys = Constants.CUSTOM;
    private String cmsURL = Constants.DEFAULT_URL;
    private String libSys = Constants.CUSTOM;
    private String libURL = Constants.DEFAULT_URL;

    public static UniversityInfo getDefault() {
        return new UniversityInfo();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCmsSys() {
        return cmsSys;
    }

    public void setCmsSys(String cmsSys) {
        this.cmsSys = cmsSys;
    }

    public String getCmsURL() {
        return cmsURL;
    }

    public void setCmsURL(String cmsURL) {
        this.cmsURL = cmsURL;
    }

    public String getLibSys() {
        return libSys;
    }

    public void setLibSys(String libSys) {
        this.libSys = libSys;
    }

    public String getLibURL() {
        return libURL;
    }

    public void setLibURL(String libURL) {
        this.libURL = libURL;
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }

    @Override
    public int compareTo(@NonNull UniversityInfo o) {
        CharacterParser parser = CharacterParser.getInstance();
        String me = parser.getSpelling(name);
        String that = parser.getSpelling(o.name);
        return me.compareTo(that);
    }
}