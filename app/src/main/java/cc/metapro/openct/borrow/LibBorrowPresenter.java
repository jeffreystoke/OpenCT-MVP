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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;


class LibBorrowPresenter implements LibBorrowContract.Presenter {

    private LibBorrowContract.View mLibBorrowView;
    private List<BorrowInfo> mBorrows;
    private Context mContext;

    LibBorrowPresenter(@NonNull LibBorrowContract.View libBorrowView, Context context) {
        mLibBorrowView = libBorrowView;
        mContext = context;

        mLibBorrowView.setPresenter(this);
    }

    @Override
    public void loadOnline(final String code) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_borrow_info).show();
        Observable
                .create(new ObservableOnSubscribe<List<BorrowInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                        Map<String, String> loginMap = Loader.getLibStuInfo(mContext);
                        loginMap.put(Constants.CAPTCHA_KEY, code);
                        e.onNext(Loader.getLibrary().getBorrowInfo(loginMap));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BorrowInfo>>() {
                    @Override
                    public void accept(List<BorrowInfo> infos) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        if (infos.size() == 0) {
                            Toast.makeText(mContext, R.string.no_borrows_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            mBorrows = infos;
                            storeBorrows();
                            loadLocalBorrows();
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BorrowInfo>>() {
                    @Override
                    public List<BorrowInfo> apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                })
                .subscribe();
    }

    @Override
    public void loadLocalBorrows() {
        Observable
                .create(new ObservableOnSubscribe<List<BorrowInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BorrowInfo>> e) throws Exception {
                        DBManger manger = DBManger.getInstance(mContext);
                        List<BorrowInfo> borrowInfos = manger.getBorrowInfos();
                        e.onNext(borrowInfos);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BorrowInfo>>() {
                    @Override
                    public void accept(List<BorrowInfo> infos) throws Exception {
                        if (infos.size() == 0) {
                            Toast.makeText(mContext, R.string.no_local_borrows_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            mBorrows = infos;
                            mLibBorrowView.onLoadBorrows(mBorrows);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BorrowInfo>>() {
                    @Override
                    public List<BorrowInfo> apply(Throwable throwable) throws Exception {
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
    public void loadCaptcha(final TextView view) {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter e) throws Exception {
                        Loader.getLibrary().getCAPTCHA();
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "获取验证码失败\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return "";
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        if (drawable != null) {
                            view.setBackground(drawable);
                            view.setText("");
                        }
                    }
                })
                .subscribe();
    }

    @Override
    public void storeBorrows() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            manger.updateBorrowInfos(mBorrows);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void start() {
        loadLocalBorrows();
    }
}
