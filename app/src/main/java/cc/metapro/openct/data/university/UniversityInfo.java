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
import android.support.annotation.NonNull;

import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.CharacterParser;

@Keep
public class UniversityInfo implements Comparable<UniversityInfo> {

    public String name, cmsSys, cmsURL, libSys, libURL;

    public UniversityInfo() {

    }

    public UniversityInfo(String name, String cmsSys, String cmsURL, String libSys, String libURL) {
        this.name = name;
        this.cmsSys = cmsSys;
        this.cmsURL = cmsURL;
        this.libSys = libSys;
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