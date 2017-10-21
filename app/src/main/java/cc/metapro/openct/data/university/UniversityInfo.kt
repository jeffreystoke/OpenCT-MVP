package cc.metapro.openct.data.university

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

import cc.metapro.openct.utils.CharacterParser
import cc.metapro.openct.utils.Constants

class UniversityInfo : Comparable<UniversityInfo> {

    var name = Constants.DEFAULT_SCHOOL_NAME
    var cmsSys = Constants.CUSTOM
    var cmsURL = Constants.DEFAULT_URL
    var libSys = Constants.CUSTOM
    var libURL = Constants.DEFAULT_URL

    override fun toString(): String {
//        return StoreHelper.toJson(this)
        return ""
    }

    override fun compareTo(o: UniversityInfo): Int {
        val parser = CharacterParser.instance
        val me = parser.getSpelling(name)
        val that = parser.getSpelling(o.name)
        return me.compareTo(that)
    }

    companion object {

        val default: UniversityInfo
            get() = UniversityInfo()
    }
}