package cc.metapro.openct.allclasses

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
import android.os.Environment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import cc.metapro.openct.R
import cc.metapro.openct.data.source.LocalHelper
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.ICalHelper
import cc.metapro.openct.utils.PrefHelper
import cc.metapro.openct.utils.base.MyObserver
import cc.metapro.openct.widget.DailyClassWidget
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.Calendar
import net.fortuna.ical4j.model.property.CalScale
import net.fortuna.ical4j.model.property.ProdId
import net.fortuna.ical4j.model.property.Version
import java.io.File
import java.io.FileOutputStream
import java.util.*

internal class AllClassesPresenter(val mContext: Context, val mView: AllClassesContract.View) : AllClassesContract.Presenter {

    init {
        mView.setPresenter(this)
    }

    override fun subscribe() {
        loadLocalClasses()
    }

    override fun unSubscribe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Export classes to iCal format, for calendar sync
     */
    override fun exportClasses() {
        ActivityUtils.showProgressDialog(mContext, R.string.creating_class_ical)

        val observable = Observable.create(ObservableOnSubscribe<Calendar> { e ->
            var fos: FileOutputStream? = null
            try {
                val week = LocalHelper.getCurrentWeek(mContext)
                val everyClassTime = Integer.parseInt(PrefHelper.getString(mContext, R.string.pref_every_class_time, "45"))
                val restTime = Integer.parseInt(PrefHelper.getString(mContext, R.string.pref_rest_time, "10"))

                val calendar = Calendar()
                calendar.properties.add(ProdId("-//OpenCT //iCal4j 2.0//EN"))
                calendar.properties.add(Version.VERSION_2_0)
                calendar.properties.add(CalScale.GREGORIAN)
                val calendarSparseArray = LocalHelper.getClassTime(mContext)
                for (c in Constants.sClasses) {
                    try {
                        val events = ICalHelper.getClassEvents(calendarSparseArray, week, everyClassTime, restTime, c)
                        calendar.components.addAll(events)
                    } catch (ignored: Exception) {
                    }

                }
                calendar.validate()

                val downloadDir = Environment.getExternalStorageDirectory()
                if (!downloadDir.exists()) {
                    downloadDir.createNewFile()
                }

                val file = File(downloadDir, "OpenCT Classes.ics")
                fos = FileOutputStream(file)
                val calOut = CalendarOutputter()
                calOut.output(calendar, fos)
                e.onNext(calendar)
            } finally {
                if (fos != null) {
                    try {
                        fos.close()
                    } catch (e1: Exception) {
                        Log.e(TAG, e1.message, e1)
                    }

                }
            }
        })

        class m : MyObserver<Calendar>(TAG) {
            override fun onNext(t: Calendar) {
                ActivityUtils.dismissProgressDialog()
                Toast.makeText(mContext, R.string.ical_create_success, Toast.LENGTH_LONG).show()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                ActivityUtils.dismissProgressDialog()
                Toast.makeText(mContext, mContext.getString(R.string.error_creating_calendar) + e.message, Toast.LENGTH_SHORT).show()
            }
        }

        val observer = m()

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    override fun clearClasses() {
        AlertDialog.Builder(mContext)
                .setTitle(R.string.warning)
                .setMessage(R.string.clear_classes_tip)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Constants.sClasses.clear()
                    storeClasses(Classes())
                    loadLocalClasses()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
    }

    override fun loadFromExcel(f: FragmentManager) {
//        class back : ExcelDialog.ExcelCallback {
//            override fun onJsonResult(json: String) {
//                try {
//                    val excelClasses = StoreHelper.fromJsonList(json, ExcelClass::class.java)
//                    val addedClasses = Classes()
//                    val oldAllClasses = Constants.sClasses
//                    Constants.sClasses = Classes()
//                    excelClasses.mapTo(addedClasses) { it.enrichedClassInfo }
//                    AlertDialog.Builder(mContext)
//                            .setTitle(R.string.select_action)
//                            .setMessage(mContext.getString(R.string.excel_import_tip, excelClasses.size))
//                            .setPositiveButton(R.string.replace) { _, _ ->
//                                storeClasses(addedClasses)
//                                loadLocalClasses()
//                                Toast.makeText(mContext, R.string.classes_updated, Toast.LENGTH_LONG).show()
//                            }
//                            .setNeutralButton(R.string.combine) { _, _ ->
//                                oldAllClasses += addedClasses
//                                storeClasses(oldAllClasses)
//                                loadLocalClasses()
//                                Toast.makeText(mContext, R.string.classes_combined, Toast.LENGTH_LONG).show()
//                            }.show()
//                } catch (e: Exception) {
//                    Toast.makeText(mContext, mContext.getString(R.string.excel_fail_tip) + ", " + e.message, Toast.LENGTH_LONG).show()
//                }
//            }
//        }
//        ExcelDialog.newInstance(back()).show(f, "excel_dialog")
    }

    override fun storeClasses(c: Classes) {
        try {
//            mDBManger.updateClasses(c)
            DailyClassWidget.update(mContext)
        } catch (e: Exception) {
            Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
        }

    }

    private fun loadLocalClasses() {
        val observable = LocalHelper.getClasses(mContext)

        val observer = object : MyObserver<Classes>(TAG) {
            override fun onNext(t: Classes) {
                Constants.sClasses = t
                Collections.sort(Constants.sClasses)
                mView.updateClasses()
            }

            override fun onError(e: Throwable) {
                super.onError(e)
                Toast.makeText(mContext, e.message, Toast.LENGTH_LONG).show()
            }
        }

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
    }

    companion object {
        private val TAG = AllClassesPresenter::class.java.simpleName
    }

}
