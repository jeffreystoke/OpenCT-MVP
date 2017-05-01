package cc.metapro.openct.data.source.remote;

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

import java.io.IOException;
import java.util.List;

import cc.metapro.openct.LoginConfig;
import cc.metapro.openct.data.service.GitRepoService;
import cc.metapro.openct.data.service.ServiceCenter;
import cc.metapro.openct.data.source.Source;
import cc.metapro.openct.data.university.DetailCustomInfo;
import cc.metapro.openct.data.university.UniversityInfo;
import io.reactivex.Observable;

public final class RemoteSource implements Source {

    private GitRepoService mService = ServiceCenter.createOpenCTService();
    private String SCHOOL_NAME;

    public RemoteSource(String schoolName) {
        SCHOOL_NAME = schoolName;
    }

    @Override
    public List<UniversityInfo> getUniversities() throws IOException {
        return mService.getUniversityInfo().execute().body();
    }

    @Override
    public LoginConfig getLoginConfig() throws IOException {
        return mService.getLoginConfigOf(SCHOOL_NAME).execute().body();
    }

    @Override
    public Observable<DetailCustomInfo> getDetailCustomInfo() {
        return null;
    }
}
