package cc.metapro.openct.splash

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

import cc.metapro.openct.utils.Constants
import com.blankj.utilcode.util.SPUtils


internal class SplashPresenter(
        private val mSchoolView: SplashContract.SchoolView,
        cmsView: SplashContract.LoginView,
        libView: SplashContract.LoginView) : SplashContract.Presenter {

    override fun subscribe() {
    }

    override fun unSubscribe() {
    }

    init {
        mSchoolView.setPresenter(this)
        cmsView.setPresenter(this)
        libView.setPresenter(this)
    }

    override fun setSelectedSchool(name: String) {
        mSchoolView.showSelectedSchool(name)
        val sp = SPUtils.getInstance()
        sp.put(Constants.PREF_SCHOOL_NAME, name)
    }

    override fun setSelectedWeek(week: Int) {
        val currentSetWeek = System.currentTimeMillis() / (1000 * 3600 * 24 * 7)
        val sp = SPUtils.getInstance()
        sp.put(Constants.PREF_CURRENT_WEEK, "$week")
        sp.put(Constants.PREF_WEEK_SET_WEEK, "$currentSetWeek")
    }

    override fun storeCMSUserPass(username: String, password: String) {
        val sp = SPUtils.getInstance()
        sp.put(Constants.PREF_CMS_USERNAME, username)
        sp.put(Constants.PREF_CMS_PASSWORD, password)
    }

    override fun storeLibUserPass(username: String, password: String) {
        val sp = SPUtils.getInstance()
        sp.put(Constants.PREF_LIB_USERNAME, username)
        sp.put(Constants.PREF_LIB_PASSWORD, password)
    }
}
