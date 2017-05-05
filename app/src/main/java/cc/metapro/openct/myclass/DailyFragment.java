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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.RecyclerViewHelper;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;

public class DailyFragment extends Fragment implements ClassContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.empty_view)
    TextView mEmptyView;

    private Observable mObservable;

    public static DailyFragment newInstance() {
        return new DailyFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_today, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mObservable != null) {
            mObservable.subscribe();
            mObservable = null;
        }
    }

    private void showClasses() {
        mEmptyView.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showEmptyView() {
        mEmptyView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        throw new UnsupportedOperationException("Presenter not used here");
    }

    @Override
    public void showClasses(final Classes classes, final int week) {
        mObservable = Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(@NonNull ObservableEmitter observableEmitter) throws Exception {
                DailyAdapter dailyAdapter = new DailyAdapter(getContext());
                RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, dailyAdapter);

                dailyAdapter.updateTodayClasses(classes, week);
                dailyAdapter.notifyDataSetChanged();

                if (dailyAdapter.hasClassToday()) {
                    showClasses();
                } else {
                    mEmptyView.setText(PrefHelper.getString(getContext(), R.string.pref_empty_class_motto, getString(R.string.motto_default)));
                    showEmptyView();
                }
            }
        }).subscribeOn(AndroidSchedulers.mainThread());

        if (isResumed()) {
            mObservable.subscribe();
            mObservable = null;
        }
    }
}
