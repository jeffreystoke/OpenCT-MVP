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
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.item.classinfo.Classes;
import cc.metapro.openct.data.university.item.classinfo.SingleClass;
import cc.metapro.openct.utils.Constants;

public class TableFragment extends Fragment {

    private static final String KEY_TYPE = "table_view_type";
    public static final int TYPE_WEEK = 1, TYPE_SEM = 2;
    private boolean infoAdded;

    private int mType = TYPE_SEM;

    @BindView(R.id.seq)
    LinearLayout mSeq;
    @BindView(R.id.content)
    RelativeLayout mContent;
    private List<SingleClass> mClasses;

    public static TableFragment newInstance(int type) {
        Bundle args = new Bundle();
        args.putInt(KEY_TYPE, type);

        TableFragment fragment = new TableFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.viewpager_class_large, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(KEY_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!infoAdded) {
            showClasses();
        }
    }

    private void showClasses() {
        Classes classes = DBManger.getInstance(getContext()).getClasses();
        if (mType == TYPE_WEEK) {
            mClasses = classes.getWeekClasses(Loader.getCurrentWeek(getContext()));
        } else {
            mClasses = classes.getAllClasses();
        }

        if (!mClasses.isEmpty()) {
            addSeqViews();
            addContentView();
            infoAdded = true;
        }
    }

    private void addSeqViews() {
        mSeq.removeAllViews();

        for (int i = 1; i <= Constants.DAILY_CLASSES; i++) {
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
        mContent.removeAllViews();

        for (SingleClass singleClass : mClasses) {
            singleClass.addViewTo(mContent, LayoutInflater.from(getContext()));
        }
    }
}
