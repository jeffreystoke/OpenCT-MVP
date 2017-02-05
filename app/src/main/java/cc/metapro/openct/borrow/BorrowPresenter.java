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
import android.util.Log;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cc.metapro.openct.R;
import cc.metapro.openct.custom.dialogs.TableChooseDialog;
import cc.metapro.openct.customviews.LinkSelectionDialog;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
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
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> e) throws Exception {
                e.onNext(Loader.getLibrary(mContext).prepareOnlineInfo());
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
                            ActivityUtils.showCaptchaDialog(manager, BorrowPresenter.this);
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_borrows).show();
        return Observable.create(new ObservableOnSubscribe<Elements>() {
            @Override
            public void subscribe(ObservableEmitter<Elements> e) throws Exception {
                Map<String, String> loginMap = Loader.getLibStuInfo(mContext);
                loginMap.put(mContext.getString(R.string.key_captcha), code);
                Document userCenter = Loader.getLibrary(mContext).login(loginMap);
                Elements elements = UniversityUtils.getLinksByPattern(userCenter, "借阅");
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
                        if (info != null && !TextUtils.isEmpty(info.BORROW_URL_PATTERN)) {
                            Pattern pattern = Pattern.compile(info.BORROW_URL_PATTERN);
                            for (Element link : links) {
                                if (pattern.matcher(link.html()).find()) {
                                    loadTargetPage(manager, link.absUrl("href"));
                                }
                            }
                        } else {
                            LinkSelectionDialog
                                    .newInstance(LinkSelectionDialog.BORROW_URL_DIALOG, links, BorrowPresenter.this)
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
        ActivityUtils.getProgressDialog(mContext, R.string.loading_classes).show();
        return Observable.create(new ObservableOnSubscribe<Map<String, Element>>() {
            @Override
            public void subscribe(ObservableEmitter<Map<String, Element>> e) throws Exception {
                e.onNext(Loader.getLibrary(mContext).getBorrowPageTables(url));
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Map<String, Element>>() {
                    @Override
                    public void accept(Map<String, Element> map) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        DBManger manger = DBManger.getInstance(mContext);
                        final AdvancedCustomInfo customInfo = manger.getAdvancedCustomInfo(mContext);
                        if (customInfo == null || TextUtils.isEmpty(customInfo.BORROW_TABLE_ID)) {
                            TableChooseDialog
                                    .newInstance(TableChooseDialog.BORROW_TABLE_DIALOG, map, BorrowPresenter.this)
                                    .show(manager, "table_choose");
                        } else {
                            mBorrows = UniversityUtils.generateInfo(map.get(customInfo.BORROW_TABLE_ID), BorrowInfo.class);
                            storeBorrows();
                            loadLocalBorrows();
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, Map<String, Element>>() {
                    @Override
                    public Map<String, Element> apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        throwable.printStackTrace();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new HashMap<>();
                    }
                }).subscribe();
    }

    @Override
    public Disposable loadQuery(final FragmentManager manager, final String actionURL, Map<String, String> queryMap) {
        return null;
    }

    @Override
    public void loadLocalBorrows() {
        Observable
                .create(new ObservableOnSubscribe<List<BorrowInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                        DBManger manger = DBManger.getInstance(mContext);
                        List<BorrowInfo> borrows = manger.getBorrows();
                        e.onNext(borrows);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BorrowInfo>>() {
                    @Override
                    public void accept(List<BorrowInfo> infos) throws Exception {
                        if (infos.size() != 0) {
                            mBorrows = infos;
                            mLibBorrowView.showAll(mBorrows);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BorrowInfo>>() {
                    @Override
                    public List<BorrowInfo> apply(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage());
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                })
                .subscribe();
    }

    @Override
    public List<BorrowInfo> getBorrows() {
        return mBorrows;
    }

    @Override
    public void storeBorrows() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            manger.updateBorrows(mBorrows == null ? new ArrayList<BorrowInfo>(0) : mBorrows);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void start() {
        loadLocalBorrows();
    }
}
