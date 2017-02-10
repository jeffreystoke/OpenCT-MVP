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
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
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
import io.reactivex.schedulers.Schedulers;

@Keep
class BorrowPresenter implements BorrowContract.Presenter {

    private final String TAG = BorrowPresenter.class.getSimpleName();
    private BorrowContract.View mLibBorrowView;
    private List<BorrowInfo> mBorrows;
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
        ActivityUtils.getProgressDialog(mContext, R.string.preparing_school_sys_info).show();

        Observable<Boolean> observable = Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(Loader.getLibrary(mContext).prepareOnlineInfo());
                e.onComplete();
            }
        });

        Observer<Boolean> observer = new MyObserver<Boolean>(TAG) {
            @Override
            public void onNext(Boolean needCaptcha) {
                ActivityUtils.dismissProgressDialog();
                if (needCaptcha) {
                    ActivityUtils.showCaptchaDialog(manager, BorrowPresenter.this);
                } else {
                    loadUserCenter(manager, "");
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_borrows).show();

        Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
            @Override
            public void subscribe(ObservableEmitter<Document> e) throws Exception {
                Map<String, String> loginMap = Loader.getLibStuInfo(mContext);
                loginMap.put(mContext.getString(R.string.key_captcha), code);
                Document userCenter = Loader.getLibrary(mContext).login(loginMap);
                e.onNext(userCenter);
            }
        });

        Observer<Document> observer = new MyObserver<Document>(TAG) {

            @Override
            public void onNext(Document document) {
                ActivityUtils.dismissProgressDialog();
                Elements links = document.select("a");
                if (links.isEmpty()) {
                    onError(new Exception("获取图书馆用户中心失败"));
                } else {
                    Constants.checkAdvCustomInfo(mContext);
                    if (!TextUtils.isEmpty(Constants.advCustomInfo.BORROW_URL_PATTERN)) {
                        Element target = document.select("a:matches(" + Constants.advCustomInfo.BORROW_URL_PATTERN + ")").first();
                        if (target != null) {
                            loadTargetPage(manager, target.absUrl("href"));
                        } else {
                            ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_BORROW, document, BorrowPresenter.this);
                        }
                    } else {
                        ActivityUtils.showLinkSelectionDialog(manager, Constants.TYPE_BORROW, document, BorrowPresenter.this);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_target_page).show();

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
                ActivityUtils.dismissProgressDialog();
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
    public void loadLocalBorrows() {
        Observable<List<BorrowInfo>> observable = Observable.create(new ObservableOnSubscribe<List<BorrowInfo>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                DBManger manger = DBManger.getInstance(mContext);
                List<BorrowInfo> borrows = manger.getBorrows();
                e.onNext(borrows);
                e.onComplete();
            }
        });

        Observer<List<BorrowInfo>> observer = new MyObserver<List<BorrowInfo>>(TAG) {
            @Override
            public void onNext(List<BorrowInfo> borrows) {
                if (borrows.size() != 0) {
                    mBorrows = borrows;
                    mLibBorrowView.showAll(mBorrows);
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
    public List<BorrowInfo> getBorrows() {
        return mBorrows;
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
