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

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.RecyclerViewHelper
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers

class DailyFragment : Fragment(), ClassContract.View {

    @BindView(R.id.recycler_view)
    internal var mRecyclerView: RecyclerView? = null
    @BindView(R.id.empty_view)
    internal var mEmptyView: TextView? = null

    private var mObservable: Observable<*>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_class_today, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onResume() {
        super.onResume()
        if (mObservable != null) {
            mObservable!!.subscribe()
            mObservable = null
        }
    }

    private fun showClasses() {
        mEmptyView!!.visibility = View.INVISIBLE
        mRecyclerView!!.visibility = View.VISIBLE
    }

    private fun showEmptyView() {
        mEmptyView!!.visibility = View.VISIBLE
        mRecyclerView!!.visibility = View.INVISIBLE
    }

    override fun setPresenter(presenter: ClassContract.Presenter) {
        throw UnsupportedOperationException("Presenter not used here")
    }

    override fun showClasses(classes: Classes, week: Int) {
        mObservable = Observable.create(ObservableOnSubscribe<Any> {
            val dailyAdapter = DailyAdapter(context)
            RecyclerViewHelper.setRecyclerView(context, mRecyclerView!!, dailyAdapter)

            dailyAdapter.updateTodayClasses(classes, week)
            dailyAdapter.notifyDataSetChanged()

            if (dailyAdapter.hasClassToday()) {
                showClasses()
            } else {
                mEmptyView!!.text = PrefHelper.getString(context, R.string.pref_empty_class_motto, getString(R.string.motto_default))
                showEmptyView()
            }
        }).subscribeOn(AndroidSchedulers.mainThread())

        if (isResumed) {
            mObservable!!.subscribe()
            mObservable = null
        }
    }

    companion object {

        fun newInstance(): DailyFragment {
            return DailyFragment()
        }
    }
}
