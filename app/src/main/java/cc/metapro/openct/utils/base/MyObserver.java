package cc.metapro.openct.utils.base;

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

import android.util.Log;

import cc.metapro.openct.utils.ActivityUtils;
import io.reactivex.observers.ResourceObserver;


public abstract class MyObserver<T> extends ResourceObserver<T> {

    private String TAG;

    public MyObserver(String tag) {
        TAG = tag;
    }

    @Override
    public void onNext(T t) {
        ActivityUtils.dismissProgressDialog();
    }

    @Override
    public void onError(Throwable e) {
        ActivityUtils.dismissProgressDialog();
        Log.e(TAG, e.getMessage(), e);
    }

    @Override
    public void onComplete() {

    }
}
