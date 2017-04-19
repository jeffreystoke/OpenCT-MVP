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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.data.university.item.classinfo.SingleClass;
import cc.metapro.openct.myclass.ClassContract;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.PrefHelper;

public class TableFragment extends Fragment implements ClassContract.View {

    @BindView(R.id.seq)
    LinearLayout mSeq;
    @BindView(R.id.content)
    RelativeLayout mContent;

    private List<SingleClass> mClasses;

    public static TableFragment newInstance() {
        Bundle args = new Bundle();
        TableFragment fragment = new TableFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_table, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void addSeqViews() {
        final int DailyClasses = Integer
                .parseInt(PrefHelper.getString(getContext(), R.string.pref_daily_class_count, "12"));
        for (int i = 1; i <= DailyClasses; i++) {
            TextView textView = new TextView(getContext());
            textView.setText(i + "");
            textView.setGravity(Gravity.CENTER);
            textView.setMinHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
            textView.setMaxHeight(Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH);
            textView.setTextSize(10);
            mSeq.addView(textView);
        }
    }

    private void addContentView() {
        for (SingleClass singleClass : mClasses) {
            singleClass.addViewTo(mContent, LayoutInflater.from(getContext()));
        }
    }

    @Override
    public void showClasses(Classes classes, int week) {
        mClasses = classes.getWeekClasses(week);
        mContent.removeAllViews();
        mSeq.removeAllViews();

        if (!mClasses.isEmpty()) {
            addSeqViews();
            addContentView();
        }
    }

    @Override
    public void setPresenter(ClassContract.Presenter presenter) {
        throw new UnsupportedOperationException("Presenter not used here");
    }
}
