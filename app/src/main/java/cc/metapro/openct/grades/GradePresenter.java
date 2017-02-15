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
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.customviews.FormDialog;
import cc.metapro.openct.customviews.TableChooseDialog;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.grades.cet.CETService;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.utils.webutils.TableUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@Keep
class GradePresenter implements GradeContract.Presenter {

    private static final String TAG = GradePresenter.class.getSimpleName();
    private Context mContext;
    private GradeContract.View mView;
    private List<GradeInfo> mGrades;
    private DBManger mDBManger;

    GradePresenter(GradeContract.View view, Context context) {
        mContext = context;
        mView = view;
        mView.setPresenter(this);
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
                    ActivityUtils.showCaptchaDialog(manager, GradePresenter.this);
                } else {
                    loadUserCenter(manager, "");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE);
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
            public void onNext(final Document userCenterDom) {
                ActivityUtils.dismissProgressDialog();
                Constants.checkAdvCustomInfo(mContext);
                final List<String> urlPatterns = Constants.advCustomInfo.getGradeUrlPatterns();
                if (!urlPatterns.isEmpty()) {
                    if (urlPatterns.size() == 1) {
                        // fetch first page from user center, it will find the grade info page in most case
                        Element target = HTMLUtils.getElementSimilar(userCenterDom, Jsoup.parse(urlPatterns.get(0)).body().children().first());
                        if (target != null) {
                            loadTargetPage(manager, target.absUrl("href"));
                        }
                    } else if (urlPatterns.size() > 1) {
                        // fetch more page to reach class info page, especially in QZ Data Soft CMS System
                        Observable<String> extraObservable = Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> e) throws Exception {
                                CmsFactory factory = Loader.getCms(mContext);
                                Document lastDom = userCenterDom;
                                Element finalTarget = null;
                                for (String pattern : urlPatterns) {
                                    if (lastDom != null) {
                                        finalTarget = HTMLUtils.getElementSimilar(lastDom, Jsoup.parse(pattern).body().children().first());
                                    }
                                    if (finalTarget != null) {
                                        lastDom = factory.getClassPageDom(finalTarget.absUrl("href"));
                                    }
                                }
                                e.onNext(finalTarget.absUrl("href"));
                            }
                        });

                        Observer<String> extraObserver = new MyObserver<String>(TAG) {
                            @Override
                            public void onNext(String targetUrl) {
                                loadTargetPage(manager, targetUrl);
                            }
                        };

                        extraObservable.subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(extraObserver);
                    } else {
                        ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_GRADE, userCenterDom, GradePresenter.this);
                    }
                } else {
                    ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_GRADE, userCenterDom, GradePresenter.this);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE);
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_grade_page).show();
        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getCms(mContext).getGradePageDom(url));
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                ActivityUtils.dismissProgressDialog();
                FormDialog.newInstance(document, GradePresenter.this).show(manager, "form_dialog");
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE);
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_grades).show();

        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getCms(mContext).getFinalGradePageDom(actionURL, queryMap, needNewPage));
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                ActivityUtils.dismissProgressDialog();
                Constants.checkAdvCustomInfo(mContext);

                if (TextUtils.isEmpty(Constants.advCustomInfo.GRADE_TABLE_ID)) {
                    TableChooseDialog
                            .newInstance(Constants.TYPE_GRADE, document, GradePresenter.this)
                            .show(manager, "table_choose");
                } else {
                    mGrades = UniversityUtils.generateInfo(
                            TableUtils.getTablesFromTargetPage(document)
                                    .get(Constants.advCustomInfo.GRADE_TABLE_ID),
                            GradeInfo.class);
                    if (mGrades.size() == 0) {
                        Toast.makeText(mContext, R.string.grades_empty, Toast.LENGTH_LONG).show();
                    } else {
                        storeGrades();
                        loadLocalGrades();
                        Toast.makeText(mContext, R.string.load_online_grades_successful, Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_GRADE);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public void loadLocalGrades() {
        Observable<List<GradeInfo>> observable = Observable.create(new ObservableOnSubscribe<List<GradeInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                e.onNext(mDBManger.getGrades());
            }
        });

        Observer<List<GradeInfo>> observer = new MyObserver<List<GradeInfo>>(TAG) {
            @Override
            public void onNext(List<GradeInfo> enrichedClasses) {
                mGrades = enrichedClasses;
                if (mGrades.size() > 0) {
                    mView.onLoadGrades(mGrades);
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
                        mView.onLoadCETGrade(stringMap);
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
            mDBManger.updateGrades(mGrades);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void clearGrades() {
        mGrades = new ArrayList<>(0);
        storeGrades();
        loadLocalGrades();
    }

    @Override
    public void start() {
        loadLocalGrades();
    }
}
