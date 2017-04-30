package cc.metapro.openct.utils;

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
import android.support.annotation.NonNull;

import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.university.DetailCustomInfo;
import cc.metapro.openct.data.university.model.classinfo.Classes;

public final class Constants {

    public static final int WEEKS = 30;
    public static final int TYPE_CMS = 0;
    public static final int TYPE_LIB = 1;
    public static final String TYPE_CLASS = "class";
    public static final String TYPE_GRADE = "grade";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_BORROW = "borrow";
    public static final String TIME_PREFIX = "class_time_";
    public static final String ACTION_KEY = "action";
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String CAPTCHA_KEY = "captcha";
    public static final String SEARCH_TYPE_KEY = "type";
    public static final String SEARCH_CONTENT_KEY = "content";
    public static final String DEFAULT_SCHOOL_NAME = "OpenCT";
    public static final String DEFAULT_URL = "example.com";

    // encryption seed
    public static final String seed =
            "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3neDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw";

    public static final int CLASS_LENGTH = 1;

    // 正方系列
    public static final String ZFSOFT = "zfsoft";
    // 苏文
    public static final String NJSUWEN = "njsuwen";
    // 普通
    public static final String CUSTOM = "custom";
    // 强智
    public static final String QZDATASOFT = "qzdatasoft";
    // 青果
    public static final String KINGOSOFT = "kingosoft";
    // 清元优软
    public static final String URP = "urp";

    // 图书馆
    // 汇文
    public static final String NJHUIWEN = "njhuiwen";

    // captcha cache file path
    public static String CAPTCHA_FILE;
    // table choose dialog options
    public static String NAME;
    public static String TIME;
    public static String TYPE;
    public static String DURING;
    public static String PLACE;
    public static String TEACHER;

    public static int CLASS_WIDTH = 0;
    public static int CLASS_BASE_HEIGHT = 0;

    @NonNull
    public static Classes sClasses = new Classes();


    public static DetailCustomInfo sDetailCustomInfo;

    public static void checkAdvCustomInfo(Context context) {
        sDetailCustomInfo = DBManger.getDetailCustomInfo(context);
    }
}
