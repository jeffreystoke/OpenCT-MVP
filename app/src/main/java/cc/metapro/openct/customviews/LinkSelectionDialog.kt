package cc.metapro.openct.customviews

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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.Toast
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.DBManger
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.Constants.TYPE_BORROW
import cc.metapro.openct.utils.Constants.TYPE_CLASS
import cc.metapro.openct.utils.Constants.TYPE_GRADE
import cc.metapro.openct.utils.Constants.sDetailCustomInfo
import cc.metapro.openct.utils.base.BaseDialog
import cc.metapro.openct.utils.base.LoginPresenter
import cc.metapro.openct.utils.base.MyObserver
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements


class LinkSelectionDialog : BaseDialog() {
    private var mTarget: Element? = null
    private var mElements: Elements? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = ActivityUtils
                .getAlertBuilder(activity, R.string.target_selection)
                .setNeutralButton(R.string.not_in_range_above, null)
        if (!sIsFirst) {
            builder.setNeutralButton(R.string.click_to_go, null)
        }

        setView(builder)
        val alertDialog = builder.create()

        alertDialog.setOnShowListener {
            val positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            val neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)

            positiveButton.setOnClickListener(View.OnClickListener {
                if (mTarget == null) {
                    Toast.makeText(activity, "Please select a link first", Toast.LENGTH_LONG).show()
                    return@OnClickListener
                }

                setUrlPattern()
                DBManger.getInstance(activity).updateAdvCustomInfo(sDetailCustomInfo)
                Constants.checkAdvCustomInfo(activity)
                mPresenter!!.loadTargetPage(fragmentManager, mTarget!!.absUrl("href"))

                TYPE = null
                mPresenter = null
                DOCUMENT = null
                dismiss()
            })

            if (sIsFirst) {
                neutralButton.setOnClickListener {
                    newInstance(TYPE!!, DOCUMENT!!, mPresenter!!, false, true)
                            .show(fragmentManager, "link_selection")
                    dismiss()
                }
            } else {
                neutralButton.setOnClickListener(View.OnClickListener {
                    if (mTarget == null) {
                        Toast.makeText(activity, "Please select a link first", Toast.LENGTH_LONG).show()
                        return@OnClickListener
                    }

                    setUrlPattern()
                    if (TYPE_GRADE == TYPE || TYPE_CLASS == TYPE) {
                        val observable = Observable.create(ObservableOnSubscribe<Document> { e ->
                            val factory = LocalHelper.getCms(activity)
                            e.onNext(factory.getPageDom(mTarget!!.absUrl("href"))!!)
                        })

                        val observer = object : MyObserver<Document>(TAG) {
                            override fun onNext(t: Document) {
                                newInstance(TYPE!!, t, mPresenter!!, false, false)
                                        .show(fragmentManager, "link_selection")
                                dismiss()
                            }
                        }

                        observable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(observer)
                    }
                })
            }
        }

        return alertDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Constants.checkAdvCustomInfo(activity)
    }

    private fun setView(builder: AlertDialog.Builder) {
        if (!sShowAll) {
            when (TYPE) {
                TYPE_CLASS -> mElements = DOCUMENT!!.select("a:matches(课表|课程)")
                TYPE_GRADE -> mElements = DOCUMENT!!.select("a:matches(成绩)")
                TYPE_BORROW -> mElements = DOCUMENT!!.select("a:matches(借阅)")
                else -> throw UnsupportedOperationException("not supported operation")
            }
        } else {
            mElements = DOCUMENT!!.select("a")
        }

        val strings = arrayOfNulls<String>(mElements!!.size)
        var i = 0
        for (e in mElements!!) {
            strings[i++] = e.text()
        }

        builder.setSingleChoiceItems(strings, -1) { dialog, which -> mTarget = mElements!![which] }
    }

    private fun setUrlPattern() {
        if (TYPE_CLASS == TYPE) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstClassUrlPattern(mTarget!!.toString())
            } else {
                sDetailCustomInfo.addClassUrlPattern(mTarget!!.toString())
            }
        } else if (TYPE_GRADE == TYPE) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstGradeUrlPattern(mTarget!!.toString())
            } else {
                sDetailCustomInfo.addGradeUrlPattern(mTarget!!.toString())
            }
        } else if (TYPE_BORROW == TYPE) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstBorrowPattern(mTarget!!.toString())
            } else {
                sDetailCustomInfo.addBorrowPattern(mTarget!!.toString())
            }
        }
    }

    companion object {

        private val TAG = LinkSelectionDialog::class.java.simpleName

        private var TYPE: String? = null
        private var sIsFirst: Boolean = false
        private var sShowAll: Boolean = false
        private var DOCUMENT: Document? = null
        private var mPresenter: LoginPresenter? = null


        fun newInstance(type: String, document: Document,
                        presenter: LoginPresenter,
                        isFirst: Boolean, showAll: Boolean): LinkSelectionDialog {
            sIsFirst = isFirst
            sShowAll = showAll
            TYPE = type
            DOCUMENT = document
            mPresenter = presenter
            return LinkSelectionDialog()
        }
    }

}
