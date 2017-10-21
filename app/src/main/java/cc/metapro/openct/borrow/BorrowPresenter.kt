package cc.metapro.openct.borrow

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

import android.support.v4.app.FragmentManager
import cc.metapro.openct.data.university.model.BorrowInfo
import java.util.*

class BorrowPresenter(private val mLibBorrowView: BorrowContract.View) : BorrowContract.Presenter {

    private var mBorrows: List<BorrowInfo> = ArrayList()

    init {
        mLibBorrowView.setPresenter(this)
    }

    override fun subscribe() {
        loadLocalBorrows()
    }

    override fun unSubscribe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun loadOnlineInfo(f: FragmentManager) {
//        ActivityUtils.showProgressDialog(mContext, R.string.preparing_school_sys_info)
//
//        val observable = LocalHelper.prepareOnlineInfo(Constants.TYPE_LIB, mContext)
//
//        val observer = object : MyObserver<Boolean>(TAG) {
//            override fun onNext(t: Boolean) {
//                super.onNext(t)
//                if (t) {
//                    ActivityUtils.showCaptchaDialog(f, this@BorrowPresenter)
//                } else {
//                    loadUserCenter(f, "")
//                }
//            }
//
//            override fun onError(e: Throwable) {
//                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW)
//                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//
//        observable.subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeWith(observer)
    }

    override fun loadUserCenter(f: FragmentManager, code: String) {
//        ActivityUtils.showProgressDialog(mContext, R.string.loading_borrows)
//        val observable = LocalHelper.login(Constants.TYPE_LIB, mContext, code)
//
//        val observer = object : MyObserver<Document>(TAG) {
//            override fun onNext(t: Document) {
//                super.onNext(t)
//                Constants.checkAdvCustomInfo(mContext)
//                val urlPatterns = Constants.sDetailCustomInfo.borrowUrlPatterns
//                if (!urlPatterns.isEmpty()) {
//                    if (urlPatterns.size == 1) {
//                         fetch first page from user center, it will find the borrow info page for most cases
//                        val target = HTMLUtils.getElementSimilar(t, Jsoup.parse(urlPatterns[0]).body().children().first())
//                        if (target != null) {
//                            loadTargetPage(f, target.absUrl("href"))
//                        }
//                    } else if (urlPatterns.size > 1) {
//                         fetch more page to reach borrow info page
//                        val extraObservable = Observable.create(ObservableOnSubscribe<String> { e ->
//                            val factory = LocalHelper.getLibrary(mContext)
//                            var lastDom: Document? = t
//                            var finalTarget: Element? = null
//                            for (pattern in urlPatterns) {
//                                if (lastDom != null) {
//                                    finalTarget = HTMLUtils.getElementSimilar(lastDom, Jsoup.parse(pattern).body().children().first())
//                                }
//                                if (finalTarget != null) {
//                                    lastDom = factory.getBorrowPageDom(finalTarget.absUrl("href"))
//                                }
//                            }
//
//                            if (finalTarget != null) {
//                                e.onNext(finalTarget.absUrl("href"))
//                            }
//                        })
//
//                        val extraObserver = object : MyObserver<String>(TAG) {
//                            override fun onNext(t :String) {
//                                loadTargetPage(f, t)
//                            }
//                        }
//
//                        extraObservable.subscribeOn(Schedulers.io())
//                                .observeOn(AndroidSchedulers.mainThread())
//                                .subscribe(extraObserver)
//                    } else {
//                        ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_BORROW, t, this@BorrowPresenter)
//                    }
//                } else {
//                    ActivityUtils.showLinkSelectionDialog(f, Constants.TYPE_BORROW, t, this@BorrowPresenter)
//                }
//            }
//
//            override fun onError(e: Throwable) {
//                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW)
//                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//
//        observable.subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer)
    }

    override fun loadTargetPage(f: FragmentManager, url: String) {
//        ActivityUtils.showProgressDialog(mContext, R.string.loading_target_page)
//
//        val observable = Observable.create(ObservableOnSubscribe<Document> { e ->
//            e.onNext(LocalHelper.getLibrary(mContext).getBorrowPageDom(url))
//            e.onComplete()
//        })
//
//        val observer = object : MyObserver<Document>(TAG) {
//            override fun onNext(t: Document) {
//                super.onNext(t)
//                val detailCustomInfo = DBManger.getDetailCustomInfo(mContext)
//                if (!TextUtils.isEmpty(detailCustomInfo.borrowTableId)) {
//                    val map = TableUtils.getTablesFromTargetPage(t)
//                    if (!map.isEmpty()) {
//                        mBorrows = UniversityUtils.generateInfoFromTable(map[detailCustomInfo.borrowTableId], BorrowInfo::class.java)
//                        if (mBorrows.isEmpty()) {
//                            Toast.makeText(mContext, R.string.borrows_empty, Toast.LENGTH_LONG).show()
//                        } else {
//                            storeBorrows()
//                            loadLocalBorrows()
//                            Toast.makeText(mContext, R.string.load_online_borrows_successful, Toast.LENGTH_LONG).show()
//                        }
//                    } else {
//                        ActivityUtils.showTableChooseDialog(f, Constants.TYPE_BORROW, t, this@BorrowPresenter)
//                    }
//                } else {
//                    ActivityUtils.showTableChooseDialog(f, Constants.TYPE_BORROW, t, this@BorrowPresenter)
//                }
//            }
//
//            override fun onError(e: Throwable) {
//                super.onError(e)
//                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW)
//                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//
//        observable.subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer)
    }

    override fun loadQuery(manager: FragmentManager, actionURL: String, queryMap: Map<String, String>, needNewPage: Boolean) {
    }

    override fun showDue() {
        val dueInfo = ArrayList<BorrowInfo>(mBorrows.size)
        val toDay = Calendar.getInstance().time
        mBorrows.filterTo(dueInfo) { it.isExceeded(toDay) }
        mLibBorrowView.updateBorrows(dueInfo)
    }

    override fun showAll() {
        mLibBorrowView.updateBorrows(mBorrows)
    }

    override fun loadLocalBorrows() {
//        val observable = LocalHelper.getBorrows(mContext)
//
//        val observer = object : MyObserver<List<BorrowInfo>>(TAG) {
//            override fun onNext(t: List<BorrowInfo>) {
//                if (t.isNotEmpty()) {
//                    mBorrows = t
//                    mLibBorrowView.updateBorrows(mBorrows)
//                }
//            }
//
//            override fun onError(e: Throwable) {
//                super.onError(e)
//                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
//            }
//        }
//
//        observable.observeOn(AndroidSchedulers.mainThread())
//                .subscribe(observer)
    }

    override fun startFilter(f: FragmentManager) {
//        if (!mBorrows.isEmpty()) {
//            for (s in mBorrows[0].titles) {
//                list!!.add(s)
//            }
//        } else {
//            Toast.makeText(mContext, mContext.getString(R.string.borrows_cannot_filter_tip), Toast.LENGTH_LONG).show()
//        }
    }

    override fun storeBorrows() {
//        try {
//            mDBManger.updateBorrows(mBorrows)
//        } catch (e: Exception) {
//            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
//        }

    }

    companion object {

        var list: MutableList<String>? = null
    }
}
