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
import android.support.annotation.Keep;

@Keep
public final class Constants {

    public static final String BR = "(<\\s*?br\\s*?/?>)|(---+)|(◇)";

    // encryption seed
    public static final String seed =
            "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3neDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw";

    public static final String BR_REPLACER = "&";

    public static final String TITLE = "title";
    public static final String URL = "url";

    public static final int DAILY_CLASSES = 12;

    public static final int CLASS_LENGTH = 1;
    // 正方系列
    public static final String ZFSOFT = "zfsoft";
    public static final String ZFSOFT2012 = "zfsoft2012";
    public static final String ZFSOFT2008 = "zfsoft2008";
    // 苏文
    public static final String NJSUWEN = "njsuwen";
    // 强智
    public static final String QZDATASOFT = "qzdatasoft";
    // 青果
    public static final String KINGOSOFT = "kingosoft";
    // 汇文
    public static final String LIBSYS = "libsys";
    // class info background colors
    public static final String[] colorString = {
            "#8BC34A", "#03A9F4", "#FF9800", "#C5CAE9", "#FFCDD2", "#009688", "#536DFE"
    };

    public static final String[] colorStringNew = {"#968bc34a", "#96ff9800", "#962196f3", "#96607d8b", "#969c27b0", "#96f44336", "#964dd0e1", "#96d4e157"};
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

    public static int getColor(int seq) {
        return Color.parseColor(colorStringNew[seq]);
    }
}
