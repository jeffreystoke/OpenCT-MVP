package cc.metapro.openct.myclass

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
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.MyObserver
import cc.metapro.openct.utils.webutils.TableUtils
import cc.metapro.openct.widget.DailyClassWidget
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

internal class ClassPresenter(private val mView: ClassContract.View,
                              private val mContext: Context) : ClassContract.Presenter {

    override fun subscribe() {
        loadLocalClasses()
    }

    override fun unSubscribe() {
    }

    private val TAG = ClassPresenter::class.java.simpleName

    init {
        mView.setPresenter(this)
    }

    override fun loadOnlineInfo(f: FragmentManager) {
        ActivityUtils.showProgressDialog(mContext, R.string.preparing_school_sys_info)

        val observable = LocalHelper.prepareOnlineInfo(Constants.TYPE_CMS, mContext)

        val observer = object : MyObserver<Boolean>(TAG) {
            override fun onNext(t: Boolean) {
//                ActivityUtils.dismissProgressDialog()
//                if (t) {
//                    ActivityUtils.showCaptchaDialog(f, this@ClassPresenter)
//                } else {
//                    loadUserCenter(f, "")
//                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                ActivityUtils.dismissProgressDialog()
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS)
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
                val urlPatterns = Constants.sDetailCustomInfo.classUrlPatterns
                if (!urlPatterns.isEmpty()) {
                    if (urlPatterns.size == 1) {
                        // fetch first page from user center, it will find the class info page in most case
//                        val target = HTMLUtils.getElementSimilar(t, Jsoup.parse(urlPatterns[0]).body().children().first())
//                        if (target != null) {
//                            loadTargetPage(f, target.absUrl("href"))
//                        } else {
//                            ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_CLASS, t, this@ClassPresenter)
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
                            if (finalTarget != null) {
                                e.onNext(finalTarget.absUrl("href"))
                            } else {
                                e.onError(Exception("failed"))
                            }
                        })

                        val extraObserver = object : MyObserver<String>(TAG) {
                            override fun onNext(t: String) {
                                loadTargetPage(f, t)
                            }

                            override fun onError(e: Throwable) {
                                super.onError(e)
                                Toast.makeText(mContext, R.string.can_not_fetch_target_page, Toast.LENGTH_LONG).show()
//                                ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_CLASS, t, this@ClassPresenter)
                            }
                        }

                        extraObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(extraObserver)
                    } else {
//                        ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_CLASS, t, this@ClassPresenter)
                    }
                } else {
//                    ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_CLASS, t, this@ClassPresenter)
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadTargetPage(f: FragmentManager, url: String) {
        ActivityUtils.showProgressDialog(mContext, R.string.loading_class_page)

        val observable = Observable.create(ObservableOnSubscribe<Document> { e -> e.onNext(LocalHelper.getCms(mContext).getPageDom(url)!!) })

        val observer = object : MyObserver<Document>(TAG) {
            override fun onNext(t: Document) {
                super.onNext(t)
//                FormDialog.newInstance(t, this@ClassPresenter).show(f, "form_dialog")
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadQuery(manager: FragmentManager, actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean) {
        ActivityUtils.showProgressDialog(mContext, R.string.loading_classes)

        val observable = Observable.create(ObservableOnSubscribe<Document> { e -> e.onNext(LocalHelper.getCms(mContext).queryClassPageDom(actionURL, queryMap, needNewPage)) })

        val observer = object : MyObserver<Document>(TAG) {
            override fun onNext(t: Document) {
                super.onNext(t)
                Constants.checkAdvCustomInfo(mContext)
                val tableId = Constants.sDetailCustomInfo.mClassTableInfo.mClassTableID
                if (TextUtils.isEmpty(tableId)) {
//                    ActivityUtils.showTableChooseDialog(manager, Constants.TYPE_CLASS, t, this@ClassPresenter)
                } else {
                    val map = TableUtils.getTablesFromTargetPage(t)
                    val rawClasses = UniversityUtils.getRawClasses(map[tableId], mContext)
                    Constants.sClasses = UniversityUtils.generateClasses(mContext, rawClasses, Constants.sDetailCustomInfo.mClassTableInfo)
                    if (Constants.sClasses.size == 0) {
                        Toast.makeText(mContext, R.string.classes_empty, Toast.LENGTH_LONG).show()
                    } else {
                        storeClasses()
                        loadLocalClasses()
                        Toast.makeText(mContext, R.string.load_online_classes_successful, Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onError(e: Throwable) {
                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun loadLocalClasses() {
        val observable = LocalHelper.getClasses(mContext)

        val observer = object : MyObserver<Classes>(TAG) {
            override fun onNext(t: Classes) {
                Constants.sClasses = t
                try {
                    mView.showClasses(Constants.sClasses, LocalHelper.getCurrentWeek(mContext))
                } catch (e: Exception) {
                    e.printStackTrace()
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

    private fun storeClasses() {
        try {
//            val manger = DBManger.getInstance(mContext)
//            manger?.updateClasses(Constants.sClasses)
            DailyClassWidget.update(mContext)
        } catch (e: Exception) {
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
        }

    }
}
