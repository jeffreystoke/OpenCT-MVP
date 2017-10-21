package cc.metapro.openct.search

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
import android.widget.Toast
import cc.metapro.openct.R
import cc.metapro.openct.data.source.LocalHelper
import cc.metapro.openct.data.university.LibraryFactory
import cc.metapro.openct.data.university.model.BookInfo
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.MyObserver
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

internal class LibSearchPresenter(private val mLibSearchView: LibSearchContract.View, private val mContext: Context) : LibSearchContract.Presenter {

    override fun subscribe() {
    }

    override fun unSubscribe() {
    }

    private val mLibraryFactory: LibraryFactory

    init {
        mLibSearchView.setPresenter(this)
        mLibraryFactory = LocalHelper.getLibrary(mContext)
    }

    override fun search(type: String, content: String) {
        mLibSearchView.showOnSearching()

        ActivityUtils.showProgressDialog(mContext, R.string.searching_library)

        val observable = Observable.create(ObservableOnSubscribe<List<BookInfo>> { e ->
            val map = HashMap<String, String>(2)
            map.put(Constants.SEARCH_TYPE_KEY, type)
            map.put(Constants.SEARCH_CONTENT_KEY, content)
            e.onNext(mLibraryFactory.search(map))
        })

        val observer = object : MyObserver<List<BookInfo>>(TAG) {
            override fun onNext(t: List<BookInfo>) {
                super.onNext(t)
                mLibSearchView.onSearchResult(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_SHORT).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(observer)
    }

    override fun nextPage() {
        mLibSearchView.showOnSearching()
        val observable = Observable.create(ObservableOnSubscribe<List<BookInfo>> { e -> e.onNext(mLibraryFactory.nextPage) })

        val observer = object : MyObserver<List<BookInfo>>(TAG) {
            override fun onNext(t: List<BookInfo>) {
                mLibSearchView.onNextPageResult(t)
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    companion object {
        private val TAG = LibSearchPresenter::class.java.name
    }
}
