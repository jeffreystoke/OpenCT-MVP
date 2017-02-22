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
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.classinfo.ExcelClass;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.ICalHelper;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static cc.metapro.openct.allclasses.AllClassesActivity.allClasses;

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

    @Override
    public void exportClasses() {
        ActivityUtils.getProgressDialog(mContext, R.string.creating_class_ical).show();
        Observable<Calendar> observable = Observable.create(new ObservableOnSubscribe<Calendar>() {
            @Override
            public void subscribe(ObservableEmitter<Calendar> e) throws Exception {
                FileOutputStream fos = null;
                try {
                    int week = Loader.getCurrentWeek(mContext);
                    Calendar calendar = new Calendar();
                    calendar.getProperties().add(new ProdId("-//OpenCT Jeff//iCal4j 2.0//EN"));
                    calendar.getProperties().add(Version.VERSION_2_0);
                    calendar.getProperties().add(CalScale.GREGORIAN);
                    SparseArray<java.util.Calendar> calendarSparseArray = Loader.getClassTime(mContext);
                    for (EnrichedClassInfo c : allClasses) {
                        try {
                            List<VEvent> events = ICalHelper.getClassEvents(calendarSparseArray, week, c);
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
                Toast.makeText(mContext, "创建日历信息时发生了异常\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @Override
    public void clearClasses() {
        new AlertDialog.Builder(mContext)
                .setTitle("警告")
                .setMessage("该操作将删除所有课程信息, 是否继续?")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        storeClasses(null);
                        loadLocalClasses();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void loadFromExcel(FragmentManager manager) {
        ExcelDialog.newInstance(new ExcelDialog.ExcelCallback() {
            @Override
            public void onJsonResult(String json) {
                List<ExcelClass> excelClasses = StoreHelper.fromJsonList(json, ExcelClass.class);
                final Classes addedClasses = new Classes();
                final Classes oldAllClasses = allClasses;
                allClasses = null;
                for (ExcelClass excelClass : excelClasses) {
                    addedClasses.add(excelClass.getEnrichedClassInfo());
                }
                new AlertDialog.Builder(mContext)
                        .setTitle("选择操作")
                        .setMessage("共有 " + addedClasses.size() + " 门课程信息, 您希望与当前课程合并还是完全使用新添加的课程?")
                        .setPositiveButton("替换", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                storeClasses(addedClasses);
                                loadLocalClasses();
                                Toast.makeText(mContext, "课程信息已更新", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNeutralButton("合并", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (EnrichedClassInfo info : addedClasses) {
                                    oldAllClasses.add(info);
                                }
                                storeClasses(oldAllClasses);
                                loadLocalClasses();
                                Toast.makeText(mContext, "课程信息已合并", Toast.LENGTH_LONG).show();
                            }
                        }).show();
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
        Observable<Classes> observable = Observable.create(new ObservableOnSubscribe<Classes>() {
            @Override
            public void subscribe(ObservableEmitter<Classes> e) throws Exception {
                e.onNext(mDBManger.getClasses());
            }
        });

        Observer<Classes> observer = new MyObserver<Classes>(TAG) {
            @Override
            public void onNext(Classes enrichedClasses) {
                try {
                    mView.updateClasses(enrichedClasses);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

}
