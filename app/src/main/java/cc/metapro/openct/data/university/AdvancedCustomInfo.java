package cc.metapro.openct.data.university;

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

import cc.metapro.openct.custom.CustomConfiguration;
import cc.metapro.openct.data.source.StoreHelper;

public class AdvancedCustomInfo {

    public CustomConfiguration mWebScriptConfiguration;

    public CmsFactory.ClassTableInfo mClassTableInfo;

    public String mCmsClassURL;

    public String mCmsGradeURL;

    public AdvancedCustomInfo() {

    }

    public void setClassTableInfo(CmsFactory.ClassTableInfo classTableInfo) {
        mClassTableInfo = classTableInfo;
    }

    public void setWebScriptConfiguration(CustomConfiguration webScriptConfiguration) {
        mWebScriptConfiguration = webScriptConfiguration;
    }

    public void setCmsClassURL(String cmsClassURL) {
        mCmsClassURL = cmsClassURL;
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }
}
