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

package cc.metapro.openct.splash

import cc.metapro.openct.utils.base.BasePresenter
import cc.metapro.openct.utils.base.BaseView

interface SplashContract {
    interface LoginView : BaseView<Presenter>

    interface SchoolView : BaseView<Presenter> {
        fun showSelectedSchool(name: String)
    }

    interface Presenter : BasePresenter {
        fun setSelectedSchool(name: String)
        fun setSelectedWeek(week: Int)
        fun storeCMSUserPass(username: String, password: String)
        fun storeLibUserPass(username: String, password: String)
    }
}