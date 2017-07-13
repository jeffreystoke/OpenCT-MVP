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
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.data.university.model.classinfo.SingleClass
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.PrefHelper
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers

class TableFragment : Fragment(), ClassContract.View {

    @BindView(R.id.seq)
    internal var mSeq: LinearLayout? = null
    @BindView(R.id.content)
    internal var mContent: RelativeLayout? = null

    private var mClasses: List<SingleClass>? = null
    private var mObservable: Observable<*>? = null
    private var mWeek: Int = 0

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_class_table, container, false)
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

    private fun addSeqViews() {
        val DailyClasses = Integer.parseInt(PrefHelper.getString(context, R.string.pref_daily_class_count, "12"))
        for (i in 1..DailyClasses) {
            val textView = TextView(context)
            textView.text = i.toString() + ""
            textView.gravity = Gravity.CENTER
            textView.minHeight = Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH
            textView.maxHeight = Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH
            textView.textSize = 10f
            mSeq!!.addView(textView)
        }
    }

    private fun addContentView() {
        val showAll = PrefHelper.getBoolean(context, R.string.pref_show_all_classes, true)
        mClasses!!
                .filterNot { !showAll && !it.inSameWeek(mWeek) }
                .forEach { it.addViewTo(mContent!!, LayoutInflater.from(context)) }
    }

    override fun showClasses(classes: Classes, week: Int) {
        mWeek = week
        mObservable = Observable.create(ObservableOnSubscribe<Any?> {
            mClasses = classes.getWeekClasses(week)
            mContent!!.removeAllViews()
            mSeq!!.removeAllViews()

            if (!mClasses!!.isEmpty()) {
                addSeqViews()
                addContentView()
            }
        }).subscribeOn(AndroidSchedulers.mainThread())

        if (isResumed) {
            mObservable!!.subscribe()
            mObservable = null
        }
    }

    override fun setPresenter(p: ClassContract.Presenter) {
        throw UnsupportedOperationException("Presenter not used here")
    }

    companion object {

        fun newInstance(): TableFragment {
            return TableFragment()
        }
    }
}
