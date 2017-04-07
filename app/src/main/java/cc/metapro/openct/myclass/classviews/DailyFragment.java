package cc.metapro.openct.myclass.classviews;

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
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.myclass.ClassContract;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.RecyclerViewHelper;


public class DailyFragment extends Fragment implements ClassContract.View {

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.empty_view)
    TextView mEmptyView;

    private DailyClassAdapter mDailyClassAdapter;

    public static DailyFragment newInstance() {
        return new DailyFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_today, container, false);
        ButterKnife.bind(this, view);
        mDailyClassAdapter = new DailyClassAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mDailyClassAdapter);
        return view;
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
    public void showClasses(Classes classes, int week) {
        mDailyClassAdapter.updateTodayClasses(classes, week);
        mDailyClassAdapter.notifyDataSetChanged();

        if (mDailyClassAdapter.hasClassToday()) {
            showClasses();
        } else {
            mEmptyView.setText(PrefHelper.getString(getContext(), R.string.pref_empty_class_motto, getString(R.string.default_motto)));
            showEmptyView();
        }
    }
}
