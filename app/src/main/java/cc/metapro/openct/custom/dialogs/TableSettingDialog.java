package cc.metapro.openct.custom.dialogs;

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
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.Constants;

@Keep
public class TableSettingDialog extends DialogFragment {

    public static final String NAME = "课程名称";
    public static final String TIME = "上课时间";
    public static final String TYPE = "课程类型";
    public static final String DURING = "课程周期";
    public static final String PLACE = "上课地点";
    public static final String TEACHER = "授课教师";

    private static String[] mStrings;
    private static TableSettingCallBack mCallBack;
    @BindView(R.id.info)
    TextView mInfo;
    @BindView(R.id.options_layout)
    ViewGroup mViewGroup;
    private List<CheckBox> mCheckBoxes;
    private int mIndex = 0;

    private Map<String, Integer> mIndexMap;

    public static TableSettingDialog newInstance(String sample, TableSettingCallBack callBack) {
        sample = sample.split(Constants.BR_REPLACER + Constants.BR_REPLACER + "+")[0];
        mStrings = sample.split(Constants.BR_REPLACER);
        mCallBack = callBack;
        return new TableSettingDialog();
    }

    @OnClick(R.id.ok)
    public void confirm() {
        for (CheckBox box : mCheckBoxes) {
            if (box.isChecked()) {
                mIndexMap.put(box.getText().toString(), mIndex);
                box.setVisibility(View.GONE);
            }
            box.setChecked(false);
        }
        mIndex++;
        if (mIndex < mStrings.length) {
            mInfo.setText(mStrings[mIndex]);
        } else {
            mCallBack.onFinish(mIndexMap);
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_table_setting, container);
        ButterKnife.bind(this, view);
        mInfo.setText(mStrings[mIndex]);
        mCheckBoxes = new ArrayList<>();
        mIndexMap = new HashMap<>();
        addClassTableOptions();
        setCancelable(false);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    private void addClassTableOptions() {
        CheckBox name = new CheckBox(getActivity());
        name.setText(NAME);
        name.setGravity(Gravity.CENTER);
        mCheckBoxes.add(name);
        mViewGroup.addView(name);

        CheckBox type = new CheckBox(getActivity());
        type.setText(TYPE);
        type.setGravity(Gravity.CENTER);
        mCheckBoxes.add(type);
        mViewGroup.addView(type);

        CheckBox time = new CheckBox(getActivity());
        time.setText(TIME);
        time.setGravity(Gravity.CENTER);
        mCheckBoxes.add(time);
        mViewGroup.addView(time);

        CheckBox during = new CheckBox(getActivity());
        during.setText(DURING);
        during.setGravity(Gravity.CENTER);
        mCheckBoxes.add(during);
        mViewGroup.addView(during);

        CheckBox place = new CheckBox(getActivity());
        place.setText(PLACE);
        place.setGravity(Gravity.CENTER);
        mCheckBoxes.add(place);
        mViewGroup.addView(place);

        CheckBox teacher = new CheckBox(getActivity());
        teacher.setText(TEACHER);
        teacher.setGravity(Gravity.CENTER);
        mCheckBoxes.add(teacher);
        mViewGroup.addView(teacher);
    }

    public interface TableSettingCallBack {
        void onFinish(Map<String, Integer> indexMap);
    }
}
