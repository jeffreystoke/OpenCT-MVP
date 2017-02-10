package cc.metapro.openct.myclass;

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
import android.os.Environment;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.Version;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.utils.webutils.TableUtils;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Keep
class ClassPresenter implements ClassContract.Presenter {

    private final String TAG = ClassPresenter.class.getSimpleName();
    private ClassContract.View mView;
    private List<EnrichedClassInfo> mEnrichedClasses;
    private Context mContext;
    private DBManger mDBManger;

    ClassPresenter(@NonNull ClassContract.View view, Context context) {
        mView = view;
        mView.setPresenter(this);
        mContext = context;
        mDBManger = DBManger.getInstance(mContext);
    }

    @Override
    public Disposable loadOnlineInfo(final FragmentManager manager) {
        ActivityUtils.getProgressDialog(mContext, R.string.preparing_school_sys_info).show();
        Observable<Boolean> observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(Loader.getCms(mContext).prepareOnlineInfo());
            }
        });

        Observer<Boolean> observer = new MyObserver<Boolean>(TAG) {
            @Override
            public void onNext(Boolean needCaptcha) {
                ActivityUtils.dismissProgressDialog();
                if (needCaptcha) {
                    ActivityUtils.showCaptchaDialog(manager, ClassPresenter.this);
                } else {
                    loadUserCenter(manager, "");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadUserCenter(final FragmentManager manager, final String code) {
        ActivityUtils.getProgressDialog(mContext, R.string.login_to_system).show();
        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                Map<String, String> loginMap = Loader.getCmsStuInfo(mContext);
                loginMap.put(mContext.getString(R.string.key_captcha), code);
                e.onNext(Loader.getCms(mContext).login(loginMap));
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                ActivityUtils.dismissProgressDialog();
                AdvancedCustomInfo info = DBManger.getAdvancedCustomInfo(mContext);
                if (!TextUtils.isEmpty(info.CLASS_URL_PATTERN)) {
                    Element target = document.select("a:matches(" + info.CLASS_URL_PATTERN + ")").first();
                    if (target != null) {
                        loadTargetPage(manager, target.absUrl("href"));
                    } else {
                        ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_CLASS, document, ClassPresenter.this);
                    }
                } else {
                    ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_CLASS, document, ClassPresenter.this);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadTargetPage(final FragmentManager manager, final String url) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_class_page).show();

        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getCms(mContext).getClassPageDom(url));
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document form) {
                ActivityUtils.dismissProgressDialog();
                FormDialog.newInstance(form, ClassPresenter.this).show(manager, "form_dialog");
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadQuery(final FragmentManager manager, final String actionURL, final Map<String, String> queryMap, final boolean needNewPage) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_classes).show();
        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getCms(mContext).getFinalClassPageDom(actionURL, queryMap, needNewPage));
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                ActivityUtils.dismissProgressDialog();
                Constants.checkAdvCustomInfo(mContext);
                String tableId = Constants.advCustomInfo.mClassTableInfo.mClassTableID;
                if (TextUtils.isEmpty(tableId)) {
                    ActivityUtils.showTableChooseDialog(manager, Constants.TYPE_CLASS, document, ClassPresenter.this);
                } else {
                    Map<String, Element> map = TableUtils.getTablesFromTargetPage(document);
                    List<Element> rawClasses = UniversityUtils.getRawClasses(map.get(tableId), mContext);
                    mEnrichedClasses = UniversityUtils.generateClasses(mContext, rawClasses, Constants.advCustomInfo.mClassTableInfo);
                    if (mEnrichedClasses.size() == 0) {
                        Toast.makeText(mContext, R.string.classes_empty, Toast.LENGTH_LONG).show();
                    } else {
                        storeClasses();
                        loadLocalClasses();
                        Toast.makeText(mContext, R.string.load_online_classes_successful, Toast.LENGTH_LONG).show();
                    }

                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_CLASS);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public void loadLocalClasses() {
        Observable<List<EnrichedClassInfo>> observable = Observable.create(new ObservableOnSubscribe<List<EnrichedClassInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<EnrichedClassInfo>> e) throws Exception {
                e.onNext(mDBManger.getClasses());
            }
        });

        Observer<List<EnrichedClassInfo>> observer = new MyObserver<List<EnrichedClassInfo>>(TAG) {
            @Override
            public void onNext(List<EnrichedClassInfo> enrichedClasses) {
                mEnrichedClasses = enrichedClasses;
                if (mEnrichedClasses.size() > 0) {
                    mView.updateClasses(mEnrichedClasses);
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

    @Override
    public void storeClasses() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            manger.updateClasses(mEnrichedClasses);
            DailyClassWidget.update(mContext);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
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
                    for (EnrichedClassInfo c : mEnrichedClasses) {
                        List<ClassInfo> classes = c.getAllClasses();
                        for (ClassInfo classInfo : classes) {
                            VEvent event = classInfo.getEvent(week, c.getWeekDay());
                            if (event != null) {
                                calendar.getComponents().add(event);
                            }
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
    public void loadFromExcel() {

    }

    @Override
    public void start() {
        loadLocalClasses();
    }
}
