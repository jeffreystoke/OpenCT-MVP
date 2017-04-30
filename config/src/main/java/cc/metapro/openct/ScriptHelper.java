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

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptHelper {

    static final String FUNCTION_NAME = "getPostContent";

    public static String getPostContent(LoginConfig config, String username, String password, String extraPart) throws ScriptException, NoSuchMethodException {
        if (config == null) return "";

        String script = config.getPostContentScript();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        engine.eval(script);
        return (String) ((Invocable) engine).invokeFunction(FUNCTION_NAME, username, password, extraPart);
    }
}
