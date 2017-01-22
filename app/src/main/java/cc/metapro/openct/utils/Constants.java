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

import android.graphics.Color;

public final class Constants {

    public static final String ACTION = "action";

    public static final String BR = "<\\s*?br\\s*?/?>";

    // encryption seed
    public static final String seed =
            "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3n/eDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw";

    public static final String BR_REPLACER = "&";

    public static final String FORM_ITEMS_RE =
            "(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)";

    public static final String TITLE = "title";
    public static final String URL = "url";

    // login map keys
    public static final String USERNAME_KEY = "username";
    public static final String PASSWORD_KEY = "password";
    public static final String CAPTCHA_KEY = "captcha";

    // cet map keys
    public static final String CET_NUM_KEY = "cet_num";
    public static final String CET_NAME_KEY = "cet_name";
    public static final String CET_TYPE_KEY = "cet_type";
    public static final String CET_SCHOOL_KEY = "cet_school";
    public static final String CET_TIME_KEY = "cet_key";
    public static final String CET_GRADE_KEY = "cet_grade";

    // school cms
    // 正方系列
    public static final String ZFSOFT2012 = "zfsoft2012";
    public static final String ZFSOFT2008 = "zfsoft2008";
    // 苏文
    public static final String NJSUWEN = "njsuwen";
    // 强智
    public static final String QZDATASOFT = "qzdatasoft";
    // 青果
    public static final String KINGOSOFT = "kingosoft";

    // library system
    public static final String LIBSYS = "libsys";

    public static final String SEARCH_TYPE = "type";
    public static final String SEARCH_CONTENT = "content";

    // filename
    public static final String CAPTCHA_FILENAME = "captcha";

    // class info background colors
    public static final String[] colorString = {
            "#8BC34A", "#03A9F4", "#FF9800", "#C5CAE9", "#FFCDD2", "#009688", "#536DFE"
    };

    public static int CLASS_WIDTH = 0;
    public static int CLASS_BASE_HEIGHT = 0;

    public static String CAPTCHA_FILE;

    public static int getColor(int seq) {
        return Color.parseColor(colorString[seq]);
    }
}
