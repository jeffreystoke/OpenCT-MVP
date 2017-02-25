package cc.metapro.openct.data.openctservice;

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

import android.support.annotation.NonNull;

import java.util.List;

import cc.metapro.openct.data.university.UniversityInfo;
import cc.metapro.openct.data.university.item.RoomInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface OpenCTService {

    /**
     * generate query url like
     * http://openct.metapro.cc/emptyroom?school={school}
     */
    @GET("/emptyroom")
    Call<List<RoomInfo>> listRoomInfos(@NonNull @Query("school") String school);

    @GET("schools.json")
    Call<List<UniversityInfo>> getOnlineUniversityInfo();
}
