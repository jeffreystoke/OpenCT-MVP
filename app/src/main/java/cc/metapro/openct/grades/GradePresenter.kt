package cc.metapro.openct.grades

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
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import android.widget.Toast
import cc.metapro.openct.R
import cc.metapro.openct.data.source.LocalHelper
import cc.metapro.openct.data.university.UniversityUtils
import cc.metapro.openct.data.university.model.GradeInfo
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.MyObserver
import cc.metapro.openct.utils.webutils.TableUtils
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.util.*

internal class GradePresenter(private val mView: GradeContract.View, private val mContext: Context) : GradeContract.Presenter {

    override fun subscribe() {
        loadLocalGrades()
    }

    override fun unSubscribe() {
    }

    private lateinit var mGrades: List<GradeInfo>

    init {
        mView.setPresenter(this)
    }

    override fun loadOnlineInfo(f: FragmentManager) {
        ActivityUtils.showProgressDialog(mContext, R.string.preparing_school_sys_info)

        val observable = LocalHelper.prepareOnlineInfo(Constants.TYPE_CMS, mContext)

        val observer = object : MyObserver<Boolean>(TAG) {
            override fun onNext(t: Boolean) {
                super.onNext(t)
                if (t) {
//                    ActivityUtils.showCaptchaDialog(f, this@GradePresenter)
                } else {
                    loadUserCenter(f, "")
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadUserCenter(f: FragmentManager, code: String) {
        ActivityUtils.showProgressDialog(mContext, R.string.login_to_system)

        val observable = LocalHelper.login(Constants.TYPE_CMS, mContext, code)

        val observer = object : MyObserver<Document>(TAG) {
            override fun onNext(t: Document) {
                super.onNext(t)
                Constants.checkAdvCustomInfo(mContext)
                val urlPatterns = Constants.sDetailCustomInfo.gradeUrlPatterns
                if (!urlPatterns.isEmpty()) {
                    if (urlPatterns.size == 1) {
                        // fetch first page from user center, it will find the grade info page in most case
//                        val target = HTMLUtils.getElementSimilar(t, Jsoup.parse(urlPatterns[0]).body().children().first())
//                        if (target != null) {
//                            loadTargetPage(f, target.absUrl("href"))
//                        }
                    } else if (urlPatterns.size > 1) {
                        // fetch more page to reach class info page, especially in QZ Data Soft CMS System
                        val extraObservable = Observable.create(ObservableOnSubscribe<String> { e ->
                            val factory = LocalHelper.getCms(mContext)
                            var lastDom: Document? = t
                            var finalTarget: Element? = null
                            for (pattern in urlPatterns) {
                                if (lastDom != null) {
//                                    finalTarget = HTMLUtils.getElementSimilar(lastDom, Jsoup.parse(pattern).body().children().first())
                                }
                                if (finalTarget != null) {
                                    lastDom = factory.getPageDom(finalTarget.absUrl("href"))
                                }
                            }
                            val url = finalTarget!!.absUrl("href")
                            e.onNext(url)
                        })

                        val extraObserver = object : MyObserver<String>(TAG) {
                            override fun onNext(t: String) {
                                loadTargetPage(f, t)
                            }
                        }

                        extraObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(extraObserver)
                    } else {
//                        ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_GRADE, t, this@GradePresenter)
                    }
                } else {
//                    ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_GRADE, t, this@GradePresenter)
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadTargetPage(f: FragmentManager, url: String) {
        ActivityUtils.showProgressDialog(mContext, R.string.loading_grade_page)

        val observable = Observable.create(ObservableOnSubscribe<Document> { e -> e.onNext(LocalHelper.getCms(mContext).getPageDom(url)!!) })

        val observer = object : MyObserver<Document>(TAG) {
            override fun onNext(t: Document) {
                super.onNext(t)
//                FormDialog.newInstance(t, this@GradePresenter).show(f, "form_dialog")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadQuery(manager: FragmentManager, actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean) {
        ActivityUtils.showProgressDialog(mContext, R.string.loading_grades)

        val observable = Observable.create(ObservableOnSubscribe<Document> { e -> e.onNext(LocalHelper.getCms(mContext).queryGradePageDom(actionURL, queryMap, needNewPage)) })

        val observer = object : MyObserver<Document>(TAG) {
            override fun onNext(t: Document) {
                super.onNext(t)
                Constants.checkAdvCustomInfo(mContext)

                if (TextUtils.isEmpty(Constants.sDetailCustomInfo.gradeTableId)) {
//                    ActivityUtils.showTableChooseDialog(manager, Constants.TYPE_GRADE, t, this@GradePresenter)
                } else {
                    mGrades = UniversityUtils.generateInfoFromTable(
                            TableUtils.getTablesFromTargetPage(t)[Constants.sDetailCustomInfo.gradeTableId],
                            GradeInfo::class.java)
                    if (mGrades.size == 0) {
                        Toast.makeText(mContext, R.string.grades_empty, Toast.LENGTH_LONG).show()
                    } else {
                        storeGrades()
                        loadLocalGrades()
                        Toast.makeText(mContext, R.string.load_online_grades_successful, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE)
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadLocalGrades() {
        val observable = LocalHelper.getGrades(mContext)

        val observer = object : MyObserver<List<GradeInfo>>(TAG) {
            override fun onNext(t: List<GradeInfo>) {
                mGrades = t
                if (mGrades.isNotEmpty()) {
                    mView.onLoadGrades(mGrades)
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadCETGrade(m: Map<String, String>) {
        Observable.create(ObservableOnSubscribe<Map<String, String>> { e ->
            //            val service = ServiceCenter.createCETService()
            val queryResult = m[mContext.getString(R.string.key_ticket_num)]?.let {
                m[mContext.getString(R.string.key_full_name)]?.let { it1 ->
                    //                    service.queryCET(
//                            mContext.getString(R.string.url_chsi_referer),
//                            it,
//                            it1, "t")
//                            .execute().body()
                }
            }

//            val document = Jsoup.parse(queryResult)
//            val elements = document.select("table[class=cetTable]")
//            val targetTable = elements.first()
//            val tds = targetTable.getElementsByTag("td")
//            val name = tds[0].text()
//            val school = tds[1].text()
//            val type = tds[2].text()
//            val num = tds[3].text()
//            val time = tds[4].text()
//            val grade = tds[5].text()
//
//            val results = HashMap<String, String>(6)
//            results.put(mContext.getString(R.string.key_full_name), name)
//            results.put(mContext.getString(R.string.key_school), school)
//            results.put(mContext.getString(R.string.key_cet_type), type)
//            results.put(mContext.getString(R.string.key_ticket_num), num)
//            results.put(mContext.getString(R.string.key_cet_time), time)
//            results.put(mContext.getString(R.string.key_cet_grade), grade)

//            e.onNext(results)
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext { stringMap -> mView.onLoadCETGrade(stringMap) }
                .onErrorReturn {
                    Toast.makeText(mContext, R.string.fetch_cet_fail, Toast.LENGTH_SHORT).show()
                    HashMap()
                }
                .subscribe()
    }

    override fun storeGrades() {
        try {
//            mDBManger.updateGrades(mGrades)
        } catch (e: Exception) {
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
        }

    }

    override fun clearGrades() {
        mGrades = ArrayList<GradeInfo>(0)
        storeGrades()
        loadLocalGrades()
    }

    companion object {

        private val TAG = GradePresenter::class.java.simpleName
    }
}
