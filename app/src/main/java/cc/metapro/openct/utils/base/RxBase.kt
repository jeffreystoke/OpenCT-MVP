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

package cc.metapro.openct.utils.base

import io.reactivex.Observer
import io.reactivex.disposables.Disposable

abstract class MyObserver<T>(val tag : String) : Observer<T> {

    override fun onNext(t: T) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onError(e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onComplete() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSubscribe(d: Disposable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}