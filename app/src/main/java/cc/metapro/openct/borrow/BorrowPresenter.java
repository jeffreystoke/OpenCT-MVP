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

import android.content.Context;
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
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.DetailCustomInfo;
import cc.metapro.openct.data.university.LibraryFactory;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.model.BorrowInfo;
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

class BorrowPresenter implements BorrowContract.Presenter {

    static List<String> list;
    private final String TAG = BorrowPresenter.class.getSimpleName();
    @NonNull
    private List<BorrowInfo> mBorrows = new ArrayList<>();
    private BorrowContract.View mLibBorrowView;
    private DBManger mDBManger;
    private Context mContext;

    BorrowPresenter(@NonNull BorrowContract.View libBorrowView, Context context) {
        mLibBorrowView = libBorrowView;
        mContext = context;
        mLibBorrowView.setPresenter(this);
        mDBManger = DBManger.getInstance(context);
    }

    @Override
    public Disposable loadOnlineInfo(final FragmentManager manager) {
        ActivityUtils.showProgressDialog(mContext, R.string.preparing_school_sys_info);

        Observable<Boolean> observable = LocalHelper.prepareOnlineInfo(Constants.TYPE_LIB, mContext);

        Observer<Boolean> observer = new MyObserver<Boolean>(TAG) {
            @Override
            public void onNext(Boolean needCaptcha) {
                super.onNext(needCaptcha);
                if (needCaptcha) {
                    ActivityUtils.showCaptchaDialog(manager, BorrowPresenter.this);
                } else {
                    loadUserCenter(manager, "");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
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
        ActivityUtils.showProgressDialog(mContext, R.string.loading_borrows);
        Observable<Document> observable = LocalHelper.login(Constants.TYPE_LIB, mContext, code);

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(final Document userCenterDom) {
                super.onNext(userCenterDom);
                Constants.checkAdvCustomInfo(mContext);
                final List<String> urlPatterns = Constants.sDetailCustomInfo.getBorrowUrlPatterns();
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
                                LibraryFactory factory = LocalHelper.getLibrary(mContext);
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
        ActivityUtils.showProgressDialog(mContext, R.string.loading_target_page);

        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                e.onNext(LocalHelper.getLibrary(mContext).getBorrowPageDom(url));
                e.onComplete();
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {
            @Override
            public void onNext(Document document) {
                super.onNext(document);
                DetailCustomInfo detailCustomInfo = DBManger.getDetailCustomInfo(mContext);
                if (!TextUtils.isEmpty(detailCustomInfo.getBorrowTableId())) {
                    Map<String, Element> map = TableUtils.getTablesFromTargetPage(document);
                    if (!map.isEmpty()) {
                        mBorrows = UniversityUtils.generateInfoFromTable(map.get(detailCustomInfo.getBorrowTableId()), BorrowInfo.class);
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
        Observable<List<BorrowInfo>> observable = LocalHelper.getBorrows(mContext);

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
