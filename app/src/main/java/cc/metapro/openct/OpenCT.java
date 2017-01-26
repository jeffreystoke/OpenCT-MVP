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

import android.app.Application;
import android.support.annotation.Keep;

import cc.metapro.openct.data.source.DaggerNetComponent;
import cc.metapro.openct.data.source.NetComponent;
import cc.metapro.openct.data.source.NetModule;

@Keep
public class OpenCT extends Application {

    private NetComponent mNetComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        createComponents();
    }

    private void createComponents() {
        mNetComponent = DaggerNetComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .netModule(new NetModule())
                .build();
    }

    public NetComponent getNetComponent() {
        return mNetComponent;
    }
}
