package cc.metapro.openct.splash.schoolselection

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.DBManger
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.data.source.remote.RemoteSource
import cc.metapro.openct.data.university.UniversityInfo
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.base.MyObserver
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.util.*

internal class SchoolAdapter(context: Context) : BaseAdapter(), StickyListHeadersAdapter {

    private val mAllSchools: MutableList<CharSequence>
    private var mSchools: MutableList<CharSequence>
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    init {
        val universityList = DBManger.getInstance(context)!!.schools
        mAllSchools = ArrayList<CharSequence>()
        mSchools = ArrayList<CharSequence>()
        if (universityList.isEmpty()) {
            getRemoteSchoolList(context)
        } else {
            setSchools(universityList)
        }
    }

    private fun setSchools(universityList: List<UniversityInfo>) {
        Collections.sort(universityList)
        mAllSchools.clear()
        mSchools!!.clear()
        for (info in universityList) {
            mAllSchools.add(info.name)
            mSchools!!.add(info.name)
        }
    }

    private fun getRemoteSchoolList(context: Context) {
        Observable.create(ObservableOnSubscribe<Any> {
            ActivityUtils.showProgressDialog(context, R.string.loading_university_info_list)
            val observer = object : MyObserver<List<UniversityInfo>>("Fetch_Universities") {
                override fun onNext(t: List<UniversityInfo>) {
                    super.onNext(t)
                    ActivityUtils.dismissProgressDialog()
                }
            }

            Observable.create(ObservableOnSubscribe<List<UniversityInfo>> {
                val source = RemoteSource(LocalHelper.getUniversity(context)!!.name)
                val universityList = source.universities
                DBManger.updateSchools(context, universityList)
                setSchools(universityList)
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith<Observer<List<UniversityInfo>>>(observer)
        }).subscribeOn(AndroidSchedulers.mainThread()).subscribe()
    }

    fun setTextFilter(filter: String): Observable<*> {
        return Observable.create(ObservableOnSubscribe<Any> { observableEmitter ->
            synchronized(SchoolAdapter::class.java) {
                if (TextUtils.isEmpty(filter)) {
                    mSchools = mAllSchools
                } else {
                    val t = filter.trim { it <= ' ' }
                    val chars = t.toCharArray()
                    val s = arrayOfNulls<CharSequence>(chars.size)
                    for (i in chars.indices) {
                        s[i] = chars[i] + ""
                    }

                    val targetList = ArrayList<CharSequence>().toMutableList()
                    for (tmp in mAllSchools) {
                        val match = s.any { tmp.contains(it!!) }
                        if (match) {
                            targetList.add(tmp.toString())
                        }
                    }
                    mSchools = targetList
                }
            }
            observableEmitter.onNext("")
        })
                .subscribeOn(Schedulers.io())
    }

    override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: HeaderViewHolder
        if (convertView == null) {
            holder = HeaderViewHolder()
            convertView = inflater.inflate(R.layout.item_header, parent, false)
            holder.headerText = convertView!!.findViewById<View>(R.id.header_text) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as HeaderViewHolder
        }

        val headerText = mSchools!![position].substring(0, 2)
        holder.headerText!!.text = headerText
        return convertView
    }

    override fun getHeaderId(position: Int): Long {
        return mSchools!![position].substring(0, 2).hashCode().toLong()
    }

    override fun getCount(): Int {
        return mSchools!!.size
    }

    override fun getItem(position: Int): Any {
        return mSchools!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()
            convertView = inflater.inflate(R.layout.item_floating_header, parent, false)
            holder.schoolText = convertView!!.findViewById<View>(R.id.school_name_text) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.schoolText!!.text = mSchools!![position]
        return convertView
    }

    private inner class HeaderViewHolder {
        internal var headerText: TextView? = null
    }

    private inner class ViewHolder {
        internal var schoolText: TextView? = null
    }
}
