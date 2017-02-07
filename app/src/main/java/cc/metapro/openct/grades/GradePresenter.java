package cc.metapro.openct.grades;

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
import android.support.annotation.Keep;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.R;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.customviews.LinkSelectionDialog;
import cc.metapro.openct.customviews.TableChooseDialog;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.grades.cet.CETService;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.TableUtils;
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
class GradePresenter implements GradeContract.Presenter {

    private static final String TAG = GradePresenter.class.getSimpleName();
    private Context mContext;
    private GradeContract.View mGradeFragment;
    private List<GradeInfo> mGrades;
    private DBManger mDBManger;

    GradePresenter(GradeContract.View view, Context context) {
        mContext = context;
        mGradeFragment = view;
        mGradeFragment.setPresenter(this);
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
                            ActivityUtils.showCaptchaDialog(manager, GradePresenter.this);
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
                Elements elements = UniversityUtils.getLinksByPattern(document, "成绩");
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
                        if (info != null && !TextUtils.isEmpty(info.GRADE_URL_PATTERN)) {
                            Pattern pattern = Pattern.compile(info.GRADE_URL_PATTERN);
                            for (Element link : links) {
                                if (pattern.matcher(link.html()).find()) {
                                    loadTargetPage(manager, link.absUrl("href"));
                                }
                            }
                        } else {
                            LinkSelectionDialog
                                    .newInstance(LinkSelectionDialog.GRADE_URL_DIALOG, links, GradePresenter.this)
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_grade_page).show();
        return Observable.create(new ObservableOnSubscribe<Form>() {
            @Override
            public void subscribe(ObservableEmitter<Form> e) throws Exception {
                Form form = Loader.getCms(mContext).getGradePageForm(url);
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
                        FormDialog.newInstance(classes, GradePresenter.this).show(manager, "form_dialog");
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_grades).show();
        return Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getCms(mContext).getGradePageTables(actionURL, queryMap));
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Document>() {
                    @Override
                    public void accept(Document map) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        DBManger manger = DBManger.getInstance(mContext);
                        final AdvancedCustomInfo customInfo = manger.getAdvancedCustomInfo(mContext);
                        if (customInfo == null || TextUtils.isEmpty(customInfo.GRADE_TABLE_ID)) {
                            TableChooseDialog
                                    .newInstance(TableChooseDialog.GRADE_TABLE_DIALOG, map, GradePresenter.this)
                                    .show(manager, "table_choose");
                        } else {
                            mGrades = UniversityUtils.generateInfo(TableUtils.getTablesFromTargetPage(map).get(customInfo.GRADE_TABLE_ID), GradeInfo.class);
                            storeGrades();
                            loadLocalGrades();
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, Document>() {
                    @Override
                    public Document apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new Document("http://openct.metapro.cc");
                    }
                }).subscribe();
    }

    @Override
    public void loadLocalGrades() {
        Observable
                .create(new ObservableOnSubscribe<List<GradeInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                        DBManger manger = DBManger.getInstance(mContext);
                        e.onNext(manger.getGrades());
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<GradeInfo>>() {
                    @Override
                    public void accept(List<GradeInfo> gradeInfos) throws Exception {
                        if (gradeInfos.size() != 0) {
                            mGrades = gradeInfos;
                            mGradeFragment.onLoadGrades(mGrades);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<GradeInfo>>() {
                    @Override
                    public List<GradeInfo> apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, mContext.getString(R.string.something_wrong) + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                }).subscribe();
    }

    @Override
    public void loadCETGrade(final Map<String, String> queryMap) {
        Observable.create(new ObservableOnSubscribe<Map<String, String>>() {
            @Override
            public void subscribe(ObservableEmitter<Map<String, String>> e) throws Exception {
                CETService service = ServiceGenerator.createCETService();
                String queryResult = service.queryCET(
                        mContext.getString(R.string.chsi_referer),
                        queryMap.get(mContext.getString(R.string.key_ticket_num)),
                        queryMap.get(mContext.getString(R.string.key_full_name)), "t")
                        .execute().body();

                Document document = Jsoup.parse(queryResult);
                Elements elements = document.select("table[class=cetTable]");
                Element targetTable = elements.first();
                Elements tds = targetTable.getElementsByTag("td");
                String name = tds.get(0).text();
                String school = tds.get(1).text();
                String type = tds.get(2).text();
                String num = tds.get(3).text();
                String time = tds.get(4).text();
                String grade = tds.get(5).text();

                Map<String, String> results = new HashMap<>(6);
                results.put(mContext.getString(R.string.key_full_name), name);
                results.put(mContext.getString(R.string.key_school), school);
                results.put(mContext.getString(R.string.key_cet_type), type);
                results.put(mContext.getString(R.string.key_ticket_num), num);
                results.put(mContext.getString(R.string.key_cet_time), time);
                results.put(mContext.getString(R.string.key_cet_grade), grade);

                e.onNext(results);
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(Map<String, String> stringMap) throws Exception {
                        mGradeFragment.onLoadCETGrade(stringMap);
                    }
                })
                .onErrorReturn(new Function<Throwable, Map<String, String>>() {
                    @Override
                    public Map<String, String> apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, R.string.load_cet_grade_fail, Toast.LENGTH_SHORT).show();
                        return new HashMap<>();
                    }
                })
                .subscribe();
    }

    @Override
    public void storeGrades() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            manger.updateGrades(mGrades);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void clearGrades() {
        mGrades = new ArrayList<>(0);
        storeGrades();
    }

    @Override
    public void start() {
        loadLocalGrades();
    }
}
