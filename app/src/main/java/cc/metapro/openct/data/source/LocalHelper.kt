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

package cc.metapro.openct.data.source

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
import android.util.Log
import android.util.SparseArray
import cc.metapro.openct.R
import cc.metapro.openct.data.LocalUser
import cc.metapro.openct.data.university.CmsFactory
import cc.metapro.openct.data.university.LibraryFactory
import cc.metapro.openct.data.university.UniversityInfo
import cc.metapro.openct.data.university.model.BorrowInfo
import cc.metapro.openct.data.university.model.GradeInfo
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.REHelper
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import java.util.*

object LocalHelper {

    private val TAG = LocalHelper::class.java.name
    lateinit var university: UniversityInfo
    private var needUpdateUniversity: Boolean = false

    fun needUpdateUniversity() {
        needUpdateUniversity = true
    }

    fun getUniversity(context: Context): UniversityInfo {
        checkUniversity(context)
        return university
    }

    fun getLibrary(context: Context): LibraryFactory {
        checkUniversity(context)
        return LibraryFactory(university)
    }

    fun getCms(context: Context): CmsFactory {
        checkUniversity(context)
        return CmsFactory(university)
    }

    fun login(actionType: Int, context: Context, captcha: String): Observable<Document> {
        return Observable.create(ObservableOnSubscribe<Document> { e ->
            var loginMap: MutableMap<String, String> = HashMap()
            if (actionType == Constants.TYPE_CMS) {
                val localUser = getCmsStuInfo(context)
                loginMap.put(Constants.USERNAME_KEY, localUser.username)
                loginMap.put(Constants.PASSWORD_KEY, localUser.password)
            } else if (actionType == Constants.TYPE_LIB) {
                val localUser = getLibStuInfo(context)
                loginMap.put(Constants.USERNAME_KEY, localUser.username)
                loginMap.put(Constants.PASSWORD_KEY, localUser.password)
            } else {
                loginMap = HashMap<String, String>()
            }
            loginMap.put(Constants.CAPTCHA_KEY, captcha)
            checkUniversity(context)
            val document = CmsFactory(university).login(loginMap)
            e.onNext(document)
        }).subscribeOn(Schedulers.io())
    }

    fun getClasses(context: Context): Observable<Classes> {
        return Observable.create(ObservableOnSubscribe<Classes> { e ->
        }).subscribeOn(Schedulers.io())
    }

    fun getGrades(context: Context): Observable<List<GradeInfo>> {
        return Observable.create(ObservableOnSubscribe<List<GradeInfo>> { e ->
        }).subscribeOn(Schedulers.io())
    }

    fun getBorrows(context: Context): Observable<List<BorrowInfo>> {
        return Observable.create(ObservableOnSubscribe<List<BorrowInfo>> { e ->
        }).subscribeOn(Schedulers.io())
    }

    fun prepareOnlineInfo(actionType: Int, context: Context): Observable<Boolean> {
        return Observable.create(ObservableOnSubscribe<Boolean> { e ->
            if (actionType == Constants.TYPE_CMS) {
                checkUniversity(context)
                e.onNext(CmsFactory(university).prepareOnlineInfo())
            } else if (actionType == Constants.TYPE_LIB) {
                e.onNext(LibraryFactory(university).prepareOnlineInfo())
            }
        }).subscribeOn(Schedulers.io())
    }

    private fun checkUniversity(context: Context) {
        if (needUpdateUniversity) {
            needUpdateUniversity = false
        }
    }

    fun getLibStuInfo(context: Context): LocalUser {
        val username = PrefHelper.getString(context, R.string.pref_lib_username, "")
        val password = PrefHelper.getString(context, R.string.pref_lib_password, "")
        return LocalUser(username, password)
    }

    fun getCmsStuInfo(context: Context): LocalUser {
        val username = PrefHelper.getString(context, R.string.pref_cms_username, "")
        val password = PrefHelper.getString(context, R.string.pref_cms_password, "")
        return LocalUser(username, password)
    }

    fun getCurrentWeek(context: Context): Int {
        updateWeekSeq(context)
        return Integer.parseInt(PrefHelper.getString(context, R.string.pref_current_week, "1"))
    }

    private fun updateWeekSeq(context: Context) {
        try {
            val cal = Calendar.getInstance(Locale.CHINA)
            cal.firstDayOfWeek = Calendar.MONDAY
            // 当前周是年度第几周
            val weekOfYearWhenSetCurrentWeek = cal.get(Calendar.WEEK_OF_YEAR)

            // 上次设置本周的时候是年度第几周
            val lastSetWeek = Integer.parseInt(PrefHelper.getString(context, R.string.pref_week_set_week, weekOfYearWhenSetCurrentWeek.toString() + ""))
            // 上次存储的是学期第几周
            var currentWeek = Integer.parseInt(PrefHelper.getString(context, R.string.pref_current_week, "1"))

            if (lastSetWeek in (weekOfYearWhenSetCurrentWeek + 1)..53) {
                // 跨年的情况
                if (lastSetWeek == 53) {
                    // 上次设置的时候是一年的最后一周
                    currentWeek += weekOfYearWhenSetCurrentWeek
                } else {
                    // 上次设置的时候不是最后一周
                    currentWeek += 52 - lastSetWeek + weekOfYearWhenSetCurrentWeek
                }
            } else {
                // 在年内
                currentWeek += weekOfYearWhenSetCurrentWeek - lastSetWeek
            }

            if (currentWeek > 30) {
                currentWeek = 1
            }

            PrefHelper.putString(context, R.string.pref_current_week, currentWeek.toString() + "")
            PrefHelper.putString(context, R.string.pref_week_set_week, weekOfYearWhenSetCurrentWeek.toString() + "")
        } catch (e: Exception) {
            Log.e(TAG, e.message)
        }

    }

    fun getClassTime(context: Context): SparseArray<Calendar> {
        val size = Integer.parseInt(PrefHelper.getString(context, R.string.pref_daily_class_count, "12"))
        val result = SparseArray<Calendar>(size)
        for (i in 0..size - 1) {
            var def = ""
            if (i < 10) {
                def += "0"
            }
            val time = PrefHelper.getString(context, Constants.TIME_PREFIX + i, def + i + ":00")
            val calendar = Calendar.getInstance()
            val parts = REHelper.getUserSetTime(time)
            calendar.set(Calendar.HOUR_OF_DAY, parts[0])
            calendar.set(Calendar.MINUTE, parts[1])
            calendar.set(Calendar.SECOND, 0)
            result.put(i + 1, calendar)
        }
        return result
    }

}
