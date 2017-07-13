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

import android.support.v4.app.FragmentManager

// implementing MVP design pattern with RxJava

interface BaseView<in T : BasePresenter> {
    fun setPresenter(p: T)
}

interface BasePresenter {
    fun subscribe()
    fun unSubscribe()
}

interface LoginPresenter : BasePresenter {
    fun loadOnlineInfo(f: FragmentManager)
    fun loadUserCenter(f: FragmentManager, code: String)
    fun loadTargetPage(f: FragmentManager, url: String)
    fun loadQuery(manager: FragmentManager, actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean)
}