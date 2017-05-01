package cc.metapro.openct.data.source.local;

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

import java.util.List;

import cc.metapro.openct.LoginConfig;
import cc.metapro.openct.data.source.Source;
import cc.metapro.openct.data.university.DetailCustomInfo;
import cc.metapro.openct.data.university.UniversityInfo;
import io.reactivex.Observable;

public final class LocalSource implements Source {

    private String SCHOOL_NAME;

    public LocalSource(String schoolName) {
        SCHOOL_NAME = schoolName;
    }

    @Override
    public List<UniversityInfo> getUniversities() {
        return null;
    }

    @Override
    public LoginConfig getLoginConfig() {
        return null;
    }

    @Override
    public Observable<DetailCustomInfo> getDetailCustomInfo() {
        return null;
    }
}
