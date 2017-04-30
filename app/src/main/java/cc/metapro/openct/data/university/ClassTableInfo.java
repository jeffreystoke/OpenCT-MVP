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

import cc.metapro.openct.R;
import cc.metapro.openct.utils.PrefHelper;

public class ClassTableInfo {

    public int mNameIndex, mTypeIndex, mDuringIndex, mPlaceIndex, mTimeIndex, mTeacherIndex;

    public String mClassTableID;

    // Regular Expressions for class info parse
    public String mNameRE, mTypeRE, mDuringRE, mTimeRE, mTeacherRE, mPlaceRE;

    public static ClassTableInfo getDefault(Context context) {
        ClassTableInfo info = new ClassTableInfo();
        info.mNameRE = PrefHelper.getString(context, R.string.pref_class_name_re, "");
        info.mTypeRE = PrefHelper.getString(context, R.string.pref_class_type_re, "");
        info.mDuringRE = PrefHelper.getString(context, R.string.pref_class_during_re, "\\d+-\\d+");
        info.mTimeRE = PrefHelper.getString(context, R.string.pref_class_time_re, "(\\d+,)+\\d+");
        info.mPlaceRE = PrefHelper.getString(context, R.string.pref_class_place_re, "");
        info.mTeacherRE = PrefHelper.getString(context, R.string.pref_class_teacher_re, "");
        return info;
    }
}
