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

import android.content.Context
import android.text.TextUtils
import cc.metapro.openct.R
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import com.scottyab.aescrypt.AESCrypt
import java.security.GeneralSecurityException
import java.util.*


internal class SplashPresenter(private val mContext: Context,
                               private val mSchoolView: SplashContract.SchoolView,
                               cmsView: SplashContract.LoginView,
                               libView: SplashContract.LoginView) : SplashContract.Presenter {

    override fun subscribe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unSubscribe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    init {
        mSchoolView.setPresenter(this)
        cmsView.setPresenter(this)
        libView.setPresenter(this)
    }

    override fun setSelectedSchool(name: String) {
        PrefHelper.putString(mContext, R.string.pref_school_name, name)
        mSchoolView.showSelectedSchool(name)
    }

    override fun setSelectedWeek(week: Int) {
        val currentWeekOfYear = Calendar.getInstance(Locale.CHINA)
                .get(Calendar.WEEK_OF_YEAR)
        PrefHelper.putString(mContext, R.string.pref_current_week, week.toString() + "")
        PrefHelper.putString(mContext, R.string.pref_week_set_week, currentWeekOfYear.toString() + "")
    }

    override fun storeCMSUserPass(username: String, password: String) {
        var password = password
        try {
            password = getEncryptedPassword(password)
        } catch (ignored: Exception) {

        } finally {
            PrefHelper.putString(mContext, R.string.pref_cms_username, username)
            PrefHelper.putString(mContext, R.string.pref_cms_password, password)
        }
    }

    override fun storeLibUserPass(username: String, password: String) {
        var password = password
        try {
            password = getEncryptedPassword(password)
        } catch (ignored: Exception) {

        } finally {
            PrefHelper.putString(mContext, R.string.pref_lib_username, username)
            PrefHelper.putString(mContext, R.string.pref_lib_password, password)
        }
    }

    @Throws(GeneralSecurityException::class)
    private fun getEncryptedPassword(password: String): String {
        if (TextUtils.isEmpty(password))
            throw NullPointerException("password shouldn't be empty")
        return AESCrypt.encrypt(Constants.seed, password)
    }
}
