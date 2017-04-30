package cc.metapro.openct.allclasses;

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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;
import cc.metapro.openct.data.university.model.classinfo.ExcelClass;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.ICalHelper;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.base.MyObserver;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class AllClassesPresenter implements AllClassesContract.Presenter {

    private static final String TAG = AllClassesPresenter.class.getSimpleName();
    private Context mContext;
    private AllClassesContract.View mView;
    private DBManger mDBManger;

    AllClassesPresenter(Context context, AllClassesContract.View view) {
        mContext = context;
        mDBManger = DBManger.getInstance(mContext);
        mView = view;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        loadLocalClasses();
    }

    /**
     * Export classes to iCal format, for calendar sync
     */
    @Override
    public void exportClasses() {
        ActivityUtils.showProgressDialog(mContext, R.string.creating_class_ical);

        Observable<Calendar> observable = Observable.create(new ObservableOnSubscribe<Calendar>() {
            @Override
            public void subscribe(ObservableEmitter<Calendar> e) throws Exception {
                FileOutputStream fos = null;
                try {
                    int week = LocalHelper.getCurrentWeek(mContext);
                    int everyClassTime = Integer.parseInt(PrefHelper.getString(mContext, R.string.pref_every_class_time, "45"));
                    int restTime = Integer.parseInt(PrefHelper.getString(mContext, R.string.pref_rest_time, "10"));

                    Calendar calendar = new Calendar();
                    calendar.getProperties().add(new ProdId("-//OpenCT //iCal4j 2.0//EN"));
                    calendar.getProperties().add(Version.VERSION_2_0);
                    calendar.getProperties().add(CalScale.GREGORIAN);
                    SparseArray<java.util.Calendar> calendarSparseArray = LocalHelper.getClassTime(mContext);
                    for (EnrichedClassInfo c : Constants.sClasses) {
                        try {
                            List<VEvent> events = ICalHelper.getClassEvents(calendarSparseArray, week, everyClassTime, restTime, c);
                            calendar.getComponents().addAll(events);
                        } catch (Exception ignored) {

                        }
                    }
                    calendar.validate();

                    File downloadDir = Environment.getExternalStorageDirectory();
                    if (!downloadDir.exists()) {
                        downloadDir.createNewFile();
                    }

                    File file = new File(downloadDir, "OpenCT Classes.ics");
                    fos = new FileOutputStream(file);
                    CalendarOutputter calOut = new CalendarOutputter();
                    calOut.output(calendar, fos);
                    e.onNext(calendar);
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (Exception e1) {
                            Log.e(TAG, e1.getMessage(), e1);
                        }
                    }
                }
            }
        });

        Observer<Calendar> observer = new MyObserver<Calendar>(TAG) {
            @Override
            public void onNext(Calendar calendar) {
                ActivityUtils.dismissProgressDialog();
                Toast.makeText(mContext, R.string.ical_create_success, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.dismissProgressDialog();
                Toast.makeText(mContext, mContext.getString(R.string.error_creating_calendar) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void clearClasses() {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.warning)
                .setMessage(R.string.clear_classes_tip)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Constants.sClasses.clear();
                        storeClasses(null);
                        loadLocalClasses();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    @Override
    public void loadFromExcel(FragmentManager manager) {
        ExcelDialog.newInstance(new ExcelDialog.ExcelCallback() {
            @Override
            public void onJsonResult(String json) {
                try {
                    List<ExcelClass> excelClasses = StoreHelper.fromJsonList(json, ExcelClass.class);
                    final Classes addedClasses = new Classes();
                    final Classes oldAllClasses = Constants.sClasses;
                    Constants.sClasses = null;
                    for (ExcelClass excelClass : excelClasses) {
                        addedClasses.add(excelClass.getEnrichedClassInfo());
                    }
                    new AlertDialog.Builder(mContext)
                            .setTitle(R.string.select_action)
                            .setMessage(mContext.getString(R.string.excel_import_tip, excelClasses.size()))
                            .setPositiveButton(R.string.replace, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    storeClasses(addedClasses);
                                    loadLocalClasses();
                                    Toast.makeText(mContext, R.string.classes_updated, Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNeutralButton(R.string.combine, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    for (EnrichedClassInfo info : addedClasses) {
                                        oldAllClasses.add(info);
                                    }
                                    storeClasses(oldAllClasses);
                                    loadLocalClasses();
                                    Toast.makeText(mContext, R.string.classes_combined, Toast.LENGTH_LONG).show();
                                }
                            }).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, mContext.getString(R.string.excel_fail_tip) + ", " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        }).show(manager, "excel_dialog");
    }

    @Override
    public void storeClasses(Classes classes) {
        try {
            mDBManger.updateClasses(classes);
            DailyClassWidget.update(mContext);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadLocalClasses() {
        Observable<Classes> observable = LocalHelper.getClasses(mContext);

        Observer<Classes> observer = new MyObserver<Classes>(TAG) {
            @Override
            public void onNext(Classes enrichedClasses) {
                Constants.sClasses = enrichedClasses;
                Collections.sort(Constants.sClasses);
                mView.updateClasses();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
