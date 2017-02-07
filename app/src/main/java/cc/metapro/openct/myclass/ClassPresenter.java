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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.R;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.customviews.LinkSelectionDialog;
import cc.metapro.openct.customviews.TableChooseDialog;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.widget.DailyClassWidget;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
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
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(Loader.getCms(mContext).prepareOnlineInfo());
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean needCaptcha) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        if (needCaptcha) {
                            ActivityUtils.showCaptchaDialog(manager, ClassPresenter.this);
                        } else {
                            loadUserCenter(manager, "");
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, Boolean>() {
                    @Override
                    public Boolean apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, "获取校园信息失败\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, throwable.getMessage(), throwable);
                        return false;
                    }
                })
                .subscribe();
    }

    @Override
    public Disposable loadUserCenter(final FragmentManager manager, final String code) {
        ActivityUtils.getProgressDialog(mContext, R.string.login_to_system).show();
        return Observable.create(new ObservableOnSubscribe<Elements>() {
            @Override
            public void subscribe(ObservableEmitter<Elements> e) throws Exception {
                Map<String, String> loginMap = Loader.getCmsStuInfo(mContext);
                loginMap.put(mContext.getString(R.string.key_captcha), code);
                Document document = Loader.getCms(mContext).login(loginMap);
                Elements elements = UniversityUtils.getLinksByPattern(document, "课程|课表");
                if (!elements.isEmpty()) {
                    e.onNext(elements);
                } else {
                    e.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Elements>() {
                    @Override
                    public void accept(Elements links) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        AdvancedCustomInfo info = mDBManger.getAdvancedCustomInfo(mContext);
                        if (info != null && !TextUtils.isEmpty(info.CLASS_URL_PATTERN)) {
                            Pattern pattern = Pattern.compile(info.CLASS_URL_PATTERN);
                            for (Element link : links) {
                                if (pattern.matcher(link.html()).find()) {
                                    loadTargetPage(manager, link.absUrl("href"));
                                }
                            }
                        } else {
                            LinkSelectionDialog
                                    .newInstance(LinkSelectionDialog.CLASS_URL_DIALOG, links, ClassPresenter.this)
                                    .show(manager, "link_selection");
                        }
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, "未成功获取到跳转链接", Toast.LENGTH_LONG).show();
                    }
                })
                .onErrorReturn(new Function<Throwable, Elements>() {
                    @Override
                    public Elements apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new Elements(0);
                    }
                })
                .subscribe();
    }

    @Override
    public Disposable loadTargetPage(final FragmentManager manager, final String url) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_class_page).show();
        return Observable.create(new ObservableOnSubscribe<Form>() {
            @Override
            public void subscribe(ObservableEmitter<Form> e) throws Exception {
                Form form = Loader.getCms(mContext).getClassPageFrom(url);
                if (form != null) {
                    e.onNext(form);
                } else {
                    e.onComplete();
                }
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Form>() {
                    @Override
                    public void accept(Form classes) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        FormDialog.newInstance(classes, ClassPresenter.this).show(manager, "form_dialog");
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, "获取课表页面失败", Toast.LENGTH_LONG).show();
                    }
                })
                .onErrorReturn(new Function<Throwable, Form>() {
                    @Override
                    public Form apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new Form();
                    }
                })
                .subscribe();
    }

    @Override
    public Disposable loadQuery(final FragmentManager manager, final String actionURL, final Map<String, String> queryMap) {
//        ActivityUtils.getProgressDialog(mContext, R.string.loading_classes).show();
        return null;
//        return Observable.create(new ObservableOnSubscribe<Map<String, Element>>() {
//            @Override
//            public void subscribe(ObservableEmitter<Map<String, Element>> e) throws Exception {
//                e.onNext(Loader.getCms(mContext).getClassPageTables(actionURL, queryMap));
//                e.onComplete();
//            }
//        })
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .doOnNext(new Consumer<Map<String, Element>>() {
//                    @Override
//                    public void accept(Map<String, Element> map) throws Exception {
//                        ActivityUtils.dismissProgressDialog();
//                        DBManger manger = DBManger.getInstance(mContext);
//                        final AdvancedCustomInfo customInfo = manger.getAdvancedCustomInfo(mContext);
//                        if (customInfo == null || customInfo.mClassTableInfo == null || TextUtils.isEmpty(customInfo.mClassTableInfo.mClassTableID)) {
//                            TableChooseDialog
//                                    .newInstance(TableChooseDialog.CLASS_TABLE_DIALOG, map, ClassPresenter.this)
//                                    .show(manager, "table_choose");
//                        } else {
//                            List<Element> rawClasses = UniversityUtils.getRawClasses(map.get(customInfo.mClassTableInfo.mClassTableID), mContext);
//                            mEnrichedClasses = UniversityUtils.generateClasses(mContext, rawClasses, customInfo.mClassTableInfo);
//                            storeClasses();
//                            loadLocalClasses();
//                        }
//                    }
//                })
//                .onErrorReturn(new Function<Throwable, Map<String, Element>>() {
//                    @Override
//                    public Map<String, Element> apply(Throwable throwable) throws Exception {
//                        ActivityUtils.dismissProgressDialog();
//                        throwable.printStackTrace();
//                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
//                        return new HashMap<>();
//                    }
//                }).subscribe();
    }

    @Override
    public void loadLocalClasses() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            mEnrichedClasses = manger.getClasses();
            if (mEnrichedClasses.size() > 0) {
                mView.updateClasses(mEnrichedClasses);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
        Observable
                .create(new ObservableOnSubscribe<Calendar>() {
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

                            File file = new File(downloadDir, "OpenCT_Classes.ics");
                            fos = new FileOutputStream(file);
                            CalendarOutputter calOut = new CalendarOutputter();
                            calOut.output(calendar, fos);
                            e.onComplete();
                        } catch (Exception e1) {
                            Log.e(TAG, e1.getMessage(), e1);
                            e.onError(e1);
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
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, R.string.ical_create_success, Toast.LENGTH_LONG).show();
                    }
                })
                .onErrorReturn(new Function<Throwable, Calendar>() {
                    @Override
                    public Calendar apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Log.e(TAG, throwable.getMessage(), throwable);
                        Toast.makeText(mContext, "创建日历信息时发生了异常\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new Calendar();
                    }
                })
                .subscribe();
    }

    @Override
    public void loadFromExcel() {

    }

    @Override
    public void start() {
        loadLocalClasses();
    }
}
