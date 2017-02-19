package cc.metapro.openct.data.university.item.classinfo;

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
import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.ClassInfoHelper;

@Keep
public class ClassInfo {

    private String teacher;
    private String place;
    private Set<ClassDuring> mDuringSet = new ArraySet<>();

    public ClassInfo(String teacher, String place, Collection<ClassDuring> duringSet) {
        this.teacher = teacher;
        this.place = place;
        mDuringSet.addAll(duringSet);
    }

    public ClassInfo(String content, CmsFactory.ClassTableInfo info) {
        String[] classes = content.split(HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+");
        String s = classes[0];
        String[] tmp = s.split(HTMLUtils.BR_REPLACER);

        if (tmp.length > 0) {
            teacher = ClassInfoHelper.infoParser(info.mTeacherIndex, info.mTeacherRE, tmp);
            place = ClassInfoHelper.infoParser(info.mPlaceIndex, info.mPlaceRE, tmp);
        }
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getPlace() {
        return TextUtils.isEmpty(place) ? "" : place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public Set<ClassDuring> getDuringSet() {
        return mDuringSet;
    }

    public void addDuring(ClassDuring during) {
        mDuringSet.add(during);
    }

    public void removeDuring(ClassDuring during) {
        mDuringSet.remove(during);
    }

    public boolean isEmpty() {
        return mDuringSet == null || mDuringSet.isEmpty();
    }

    @Override
    public String toString() {
        return StoreHelper.toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ClassInfo) {
            ClassInfo c = (ClassInfo) obj;
            if (Arrays.equals(mDuringSet.toArray(new ClassDuring[0]), c.mDuringSet.toArray(new ClassDuring[0]))) {
                return true;
            }
        }
        return false;
    }
}
