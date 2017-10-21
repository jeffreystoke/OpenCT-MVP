package cc.metapro.openct.utils

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

import android.content.Context
import cc.metapro.openct.data.university.model.classinfo.Classes

object Constants {

    val PREF_CURRENT_WEEK = "current_week"
    val PREF_WEEK_SET_WEEK = "week_set_week"
    val PREF_CMS_USERNAME = "cms_username"
    val PREF_CMS_PASSWORD = "cms_password"
    val PREF_LIB_USERNAME = "lib_username"
    val PREF_LIB_PASSWORD = "lib_password"
    val PREF_SCHOOL_NAME = "school_name"

    val WEEKS = 30
    val TYPE_CMS = 0
    val TYPE_LIB = 1
//    val TYPE_CLASS = "class"
//    val TYPE_GRADE = "grade"
//    val TYPE_SEARCH = "search"
//    val TYPE_BORROW = "borrow"

    val TIME_PREFIX = "class_time_"
    val ACTION_KEY = "action"
    val USERNAME_KEY = "username"
    val PASSWORD_KEY = "password"
    val CAPTCHA_KEY = "captcha"
    val SEARCH_TYPE_KEY = "type"
    val SEARCH_CONTENT_KEY = "content"
    val DEFAULT_SCHOOL_NAME = "OpenCT"
    val DEFAULT_URL = "example.com"

    // encryption seed
    val seed = "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3neDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw"

    val CLASS_LENGTH = 1

    // 正方系列
    val ZFSOFT = "zfsoft"
    // 苏文
    val NJSUWEN = "njsuwen"
    // 普通
    val CUSTOM = "custom"
    // 强智
    val QZDATASOFT = "qzdatasoft"
    // 青果
    val KINGOSOFT = "kingosoft"
    // 清元优软
    val URP = "urp"

    // 图书馆
    // 汇文
    val NJHUIWEN = "njhuiwen"

    // captcha cache file path
    lateinit var CAPTCHA_FILE: String
    // table choose dialog options
    lateinit var NAME: String
    lateinit var TIME: String
    lateinit var TYPE: String
    lateinit var DURING: String
    lateinit var PLACE: String
    lateinit var TEACHER: String
    var CLASS_WIDTH = 0
    var CLASS_BASE_HEIGHT = 0
    var sClasses = Classes()
    lateinit var sDetailCustomInfo: DetailCustomInfo

    fun checkAdvCustomInfo(context: Context) {
//        sDetailCustomInfo = DBManger.getDetailCustomInfo(context)
    }
}
