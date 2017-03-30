package cc.metapro.openct.borrow;

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

import android.app.ProgressDialog;
import android.content.Context;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.MyObserver;
import cc.metapro.openct.utils.webutils.TableUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@Keep
class BorrowPresenter implements BorrowContract.Presenter {

    private final String TAG = BorrowPresenter.class.getSimpleName();
    @NonNull
    private List<BorrowInfo> mBorrows = new ArrayList<>();
    private BorrowContract.View mLibBorrowView;
    private DBManger mDBManger;
    private Context mContext;
    static List<String> list;

    BorrowPresenter(@NonNull BorrowContract.View libBorrowView, Context context) {
        mLibBorrowView = libBorrowView;
        mContext = context;
        mLibBorrowView.setPresenter(this);
        mDBManger = DBManger.getInstance(context);
    }

    @Override
    public Disposable loadOnlineInfo(final FragmentManager manager) {
        final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(mContext, R.string.preparing_school_sys_info);
        progressDialog.show();

        Observable<Boolean> observable = Loader.prepareOnlineInfo(Loader.ACTION_LIBRARY, mContext);

        Observer<Boolean> observer = new MyObserver<Boolean>(TAG) {
            @Override
            public void onNext(Boolean needCaptcha) {
                progressDialog.dismiss();
                if (needCaptcha) {
                    ActivityUtils.showCaptchaDialog(manager, BorrowPresenter.this);
                } else {
                    loadUserCenter(manager, "");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                progressDialog.dismiss();
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadUserCenter(final FragmentManager manager, final String code) {
        final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(mContext, R.string.loading_borrows);
        progressDialog.show();

        Observable<Document> observable = Loader.login(Loader.ACTION_LIBRARY, mContext, code);

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(final Document userCenterDom) {
                progressDialog.dismiss();
                Constants.checkAdvCustomInfo(mContext);
                final List<String> urlPatterns = Constants.advCustomInfo.getBorrowUrlPatterns();
                if (!urlPatterns.isEmpty()) {
                    if (urlPatterns.size() == 1) {
                        // fetch first page from user center, it will find the borrow info page for most cases
                        Element target = HTMLUtils.getElementSimilar(userCenterDom, Jsoup.parse(urlPatterns.get(0)).body().children().first());
                        if (target != null) {
                            loadTargetPage(manager, target.absUrl("href"));
                        }
                    } else if (urlPatterns.size() > 1) {
                        // fetch more page to reach borrow info page
                        Observable<String> extraObservable = Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> e) throws Exception {
                                LibraryFactory factory = Loader.getLibrary(mContext);
                                Document lastDom = userCenterDom;
                                Element finalTarget = null;
                                for (String pattern : urlPatterns) {
                                    if (lastDom != null) {
                                        finalTarget = HTMLUtils.getElementSimilar(lastDom, Jsoup.parse(pattern).body().children().first());
                                    }
                                    if (finalTarget != null) {
                                        lastDom = factory.getBorrowPageDom(finalTarget.absUrl("href"));
                                    }
                                }

                                if (finalTarget != null) {
                                    e.onNext(finalTarget.absUrl("href"));
                                }
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
                        ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_BORROW, userCenterDom, BorrowPresenter.this);
                    }
                } else {
                    ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_BORROW, userCenterDom, BorrowPresenter.this);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                progressDialog.dismiss();
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadTargetPage(final FragmentManager manager, final String url) {
        final ProgressDialog progressDialog = ActivityUtils.getProgressDialog(mContext, R.string.loading_target_page);
        progressDialog.show();

        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(Loader.getLibrary(mContext).getBorrowPageDom(url));
                e.onComplete();
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                progressDialog.dismiss();
                AdvancedCustomInfo customInfo = DBManger.getAdvancedCustomInfo(mContext);
                if (!TextUtils.isEmpty(customInfo.BORROW_TABLE_ID)) {
                    Map<String, Element> map = TableUtils.getTablesFromTargetPage(document);
                    if (!map.isEmpty()) {
                        mBorrows = UniversityUtils.generateInfo(map.get(customInfo.BORROW_TABLE_ID), BorrowInfo.class);
                        if (mBorrows.size() == 0) {
                            Toast.makeText(mContext, R.string.borrows_empty, Toast.LENGTH_LONG).show();
                        } else {
                            storeBorrows();
                            loadLocalBorrows();
                            Toast.makeText(mContext, R.string.load_online_borrows_successful, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        ActivityUtils.showTableChooseDialog(manager, Constants.TYPE_BORROW, document, BorrowPresenter.this);
                    }
                } else {
                    ActivityUtils.showTableChooseDialog(manager, Constants.TYPE_BORROW, document, BorrowPresenter.this);
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                progressDialog.dismiss();
                ActivityUtils.showAdvCustomTip(mContext, Constants.TYPE_BORROW);
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        return null;
    }

    @Override
    public Disposable loadQuery(final FragmentManager manager, final String actionURL, Map<String, String> queryMap, boolean needNewPage) {
        return null;
    }

    @Override
    public void showDue() {
        List<BorrowInfo> dueInfo = new ArrayList<>(mBorrows.size());
        Date toDay = Calendar.getInstance().getTime();
        for (BorrowInfo b : mBorrows) {
            if (b.isExceeded(toDay)) {
                dueInfo.add(b);
            }
        }
        mLibBorrowView.updateBorrows(dueInfo);
    }

    @Override
    public void showAll() {
        mLibBorrowView.updateBorrows(mBorrows);
    }

    @Override
    public void loadLocalBorrows() {
        Observable<List<BorrowInfo>> observable = Loader.getBorrows(mContext);

        Observer<List<BorrowInfo>> observer = new MyObserver<List<BorrowInfo>>(TAG) {
            @Override
            public void onNext(List<BorrowInfo> borrows) {
                if (borrows.size() != 0) {
                    mBorrows = borrows;
                    mLibBorrowView.updateBorrows(mBorrows);
                }
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

    @Override
    public void startFilter(FragmentManager manager) {
        if (!mBorrows.isEmpty()) {
            for (String s : mBorrows.get(0).getTitles()) {
                list.add(s);
            }
            // TODO: 17/3/18 add filter
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.borrows_cannot_filter_tip), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void storeBorrows() {
        try {
            mDBManger.updateBorrows(mBorrows);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void start() {
        loadLocalBorrows();
    }
}
