package cc.metapro.openct;

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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.Scriptable;

public class ScriptHelper {

    static final String FUNCTION_NAME = "getPostContent";
    static final String FUNCTION_STRUCTURE = "function %s(username, password, captcha, extraPart) { %s }";
    static final String POST_CONTENT = "{POST_CONTENT}";
    static final String CAPTCHA = "{CAPTCHA_CODE}";
    static final String USERNAME = "{USERNAME}";
    static final String PASSWORD = "{PASSWORD}";
    static final String EXTRA_PART = "{EXTRA_PART}";

    public static String getPostContent(LoginConfig config, String username, String password, String captcha, String extraPart) {
        if (config == null) return "";

        Context cx = Context.enter();
        cx.setOptimizationLevel(-1);
        try {
            Scriptable scope = cx.initStandardObjects();
            String script = config.getPostContentScript();
            String[] lines = script.split("\r\n|\r|\n");
            cx.evaluateString(scope, script, "ScriptAPI", lines.length, null);
            Object func = scope.get(FUNCTION_NAME, scope);
            if (func instanceof Function) {
                Function f = (Function) func;
                Object[] args = {username, password, captcha, extraPart};
                Object result = f.call(cx, scope, scope, args);
                return Context.toString(result);
            }
        } finally {
            Context.exit();
        }
        return "";
    }
}
