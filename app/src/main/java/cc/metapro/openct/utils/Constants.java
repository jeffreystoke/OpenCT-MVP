package cc.metapro.openct.utils;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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

    // encryption seed
    public static final String seed = "MGICAQACEQDkTyaa2c4v50mkZfyNT0HFAgMBAAECEDrkM9gTwLzYFoimr5b74KECCQD1rE5MzS2H7QIJAO3n/eDhgDY5AghQ4kbxQEgyTQIIYe3qGoSYgzkCCQCwrArrXqKPw";

    public static final String BR_REPLACER = "&";

    public static final String FORM_ITEMS_RE = "(select)|(input)|(textarea)|(button)|(datalist)|(keygen)|(output)";

    //
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

    // preference related
    public static final String PREF_INITED = "pref_inited";

    public static final String PREF_SCHOOL_ABBR_KEY = "pref_school_abbr";
    public static final String PREF_USE_CUSTOM = "pref_use_custom";

    public static final String PREF_CMS_USERNAME_KEY = "pref_cms_username";
    public static final String PREF_CMS_PASSWORD_KEY = "pref_cms_password";

    public static final String PREF_LIB_USERNAME_KEY = "pref_lib_username";
    public static final String PREF_LIB_PASSWORD_KEY = "pref_lib_password";

    public static final String PREF_WEEK_SET_KEY = "pref_tmp_week_set";
    public static final String PREF_CURRENT_WEEK_KEY = "current_week_seq";

    public static final String PREF_CMS_PASSWORD_ENCRYPTED = "cms_encrypted";
    public static final String PREF_LIB_PASSWORD_ENCRYPTED = "lib_encrypted";

    // school cms
    public static final String ZFSOFT = "zfsoft";
    public static final String NJSUWEN = "njsuwen";
    public static final String QZDATASOFT = "qzdatasoft";

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
    public static String CAPTCHA_FILE;

    public static int getColor(int seq) {
        return Color.parseColor(colorString[seq]);
    }
}
