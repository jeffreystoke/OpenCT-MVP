package cc.metapro.openct.room;

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
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.List;

import cc.metapro.openct.data.openctservice.OpenCTService;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.item.RoomInfo;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;

class RoomPresenter implements RoomContract.Presenter {

    private List<RoomInfo> mRoomInfos;

    private RoomContract.View mRoomFragment;

    RoomPresenter(@NonNull RoomContract.View roomFragment) {
        roomFragment.setPresenter(this);
        mRoomFragment = roomFragment;
    }

    @Override
    public List<RoomInfo> getRoomInfos() {
        return mRoomInfos;
    }

    @Override
    public void loadOnlineRoomInfos() {
        Observable.create(
                new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> e) throws Exception {
                        OpenCTService service = ServiceGenerator.createService(OpenCTService.class, ServiceGenerator.GSON_CONVERTER);

                        Call<List<RoomInfo>> call = service.listRoomInfos("njit");
                        try {
                            List<RoomInfo> contributors = call.execute().body();
                            if (contributors != null && contributors.size() != 0) {
                                mRoomInfos = contributors;
                                mRoomFragment.showRooms(-1, -1, -1, null);
                            }
                        } catch (IOException ioe) {
                            // handle errors
                        }
                    }
                }).subscribeOn(Schedulers.io())
                .subscribe();
    }

    @Override
    public void loadLocalRoomInfos(Context context) {

    }

    @Override
    public void storeRoomInfos(final Context context) {
        Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                String s = StoreHelper.getJsonText(mRoomInfos);
            }
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public void start() {

    }
}
