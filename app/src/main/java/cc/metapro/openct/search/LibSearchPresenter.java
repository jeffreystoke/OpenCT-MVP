package cc.metapro.openct.search;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.BookInfo;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

class LibSearchPresenter implements LibSearchContract.Presenter {

    private static final String TAG = "LIB_PRESENTER";

    private LibSearchContract.View mLibSearchView;

    private Spinner mSpinner;

    private EditText mEditText;

    private Context mContext;

    LibSearchPresenter(@NonNull LibSearchContract.View libSearchView, Spinner spinner, EditText editText) {
        mLibSearchView = libSearchView;
        mSpinner = spinner;
        mEditText = editText;
        mContext = mEditText.getContext();
        mLibSearchView.setPresenter(this);
    }

    @Override
    public Disposable search() {
        mLibSearchView.showOnSearching();
        return Observable
                .create(new ObservableOnSubscribe<List<BookInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                        Map<String, String> map = new HashMap<>(2);
                        map.put(Constants.SEARCH_TYPE, mSpinner.getSelectedItem().toString());
                        map.put(Constants.SEARCH_CONTENT, mEditText.getText().toString());
                        e.onNext(Loader.getLibrary().search(map));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BookInfo>>() {
                    @Override
                    public void accept(List<BookInfo> infos) throws Exception {
                        mLibSearchView.onSearchResult(infos);
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BookInfo>>() {
                    @Override
                    public List<BookInfo> apply(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        mLibSearchView.onSearchResult(new ArrayList<BookInfo>(0));
                        return new ArrayList<>();
                    }
                })
                .subscribe();
    }

    @Override
    public Disposable nextPage() {
        mLibSearchView.showOnSearching();
        return Observable
                .create(new ObservableOnSubscribe<List<BookInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BookInfo>> e) throws Exception {
                        List<BookInfo> infos = Loader.getLibrary().getNextPage();
                        e.onNext(infos);
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<BookInfo>>() {
                    @Override
                    public void accept(List<BookInfo> infos) throws Exception {
                        mLibSearchView.onNextPageResult(infos);
                    }
                })
                .onErrorReturn(new Function<Throwable, List<BookInfo>>() {
                    @Override
                    public List<BookInfo> apply(Throwable throwable) throws Exception {
                        Log.e(TAG, throwable.getMessage(), throwable);
                        Toast.makeText(mEditText.getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        mLibSearchView.onNextPageResult(new ArrayList<BookInfo>());
                        return new ArrayList<>();
                    }
                })
                .subscribe();
    }

    @Override
    public void start() {

    }
}
