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
import android.support.annotation.Keep;

import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityInfo;

@Keep
public final class Constants {

    public static final String TYPE_CLASS = "class";
    public static final String TYPE_GRADE = "grade";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_BORROW = "borrow";

    // encryption seed
    public static final String seed =
            "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3neDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw";

    public static final String TITLE = "title";
    public static final String URL = "url";

    public static final int DAILY_CLASSES = 12;
    public static final int CLASS_LENGTH = 1;
    // 正方系列
    public static final String ZFSOFT = "zfsoft";
    // 苏文
    public static final String NJSUWEN = "njsuwen";
    // 普通
    public static final String COMMON = "custom";
    // 强智
    public static final String QZDATASOFT = "qzdatasoft";
    // 青果
    public static final String KINGOSOFT = "kingosoft";
    // 清元优软
    public static final String URP = "urp";
    // 汇文
    public static final String NJHUIWEN = "njhuiwen";
    public static AdvancedCustomInfo advCustomInfo;
    // map keys
    public static String ACTION_KEY;
    public static String USERNAME_KEY;
    public static String PASSWORD_KEY;
    public static String CAPTCHA_KEY;
    public static String SEARCH_TYPE_KEY;
    public static String SEARCH_CONTENT_KEY;
    public static String CAPTCHA_FILE;
    public static int CLASS_WIDTH = 0;
    public static int CLASS_BASE_HEIGHT = 0;

    public static void checkAdvCustomInfo(Context context) {
        advCustomInfo = DBManger.getAdvancedCustomInfo(context);
    }
}
