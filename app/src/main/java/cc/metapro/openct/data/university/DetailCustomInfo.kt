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

import cc.metapro.openct.data.source.local.StoreHelper
import cc.metapro.openct.utils.Constants
import java.util.*

class DetailCustomInfo {

    lateinit var mClassTableInfo: ClassTableInfo
    var schoolName = Constants.DEFAULT_SCHOOL_NAME

    var gradeTableId = ""
    var borrowTableId = ""
    private var mClassUrlPatterns: MutableList<String> = LinkedList()
    private var mGradeUrlPatterns: MutableList<String> = LinkedList()
    private var mBorrowUrlPatterns: MutableList<String> = LinkedList()

    constructor()

    constructor(schoolName: String) {
        this.schoolName = schoolName
    }

    val classUrlPatterns: List<String>
        get() = mClassUrlPatterns

    val gradeUrlPatterns: List<String>
        get() = mGradeUrlPatterns

    val borrowUrlPatterns: List<String>
        get() = mBorrowUrlPatterns

    fun setFirstClassUrlPattern(urlPattern: String) {
        mClassUrlPatterns = ArrayList<String>()
        mClassUrlPatterns.add(urlPattern)
    }

    fun setFirstGradeUrlPattern(urlPattern: String) {
        mGradeUrlPatterns = ArrayList<String>()
        mGradeUrlPatterns.add(urlPattern)
    }

    fun setFirstBorrowPattern(urlPattern: String) {
        mBorrowUrlPatterns = ArrayList<String>()
        mBorrowUrlPatterns.add(urlPattern)
    }

    fun addClassUrlPattern(urlPattern: String) {
        mClassUrlPatterns.add(urlPattern)
    }

    fun addGradeUrlPattern(urlPattern: String) {
        mGradeUrlPatterns.add(urlPattern)
    }

    fun addBorrowPattern(urlPattern: String) {
        mBorrowUrlPatterns.add(urlPattern)
    }

    fun setClassTableInfo(classTableInfo: ClassTableInfo) {
        mClassTableInfo = classTableInfo
    }

    override fun toString(): String {
        return StoreHelper.toJson(this)
    }
}
