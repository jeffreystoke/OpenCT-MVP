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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

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
    public static final String[] titles = {NAME, TIME, TYPE, DURING, PLACE, TEACHER};
    private static final String TAG = "openct_table_setting";
    private static String[] mStrings;
    private static TableSettingCallBack mCallBack;
    @BindView(R.id.info)
    TextView mInfo;
    @BindView(R.id.options_layout)
    ViewGroup mViewGroup;
    private List<CheckBox> mCheckBoxes;
    private int mIndex = 0;

    private Map<String, Integer> mResultIndexMap;
    private Map<String, MaterialEditText> mEditTextMap;

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
                mResultIndexMap.put(box.getText().toString(), mIndex);
                box.setVisibility(View.GONE);
            }
            box.setChecked(false);
        }
        mIndex++;
        if (mIndex < mStrings.length) {
            try {
                mInfo.setText(mStrings[mIndex]);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            Map<String, String> resultReMap = new HashMap<>(mEditTextMap.size());
            for (String title : mEditTextMap.keySet()) {
                resultReMap.put(title, mEditTextMap.get(title).getText().toString());
            }
            mCallBack.onFinish(mResultIndexMap, resultReMap);
            dismiss();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_table_setting, container);
        ButterKnife.bind(this, view);
        mInfo.setText(mStrings[0]);
        mResultIndexMap = new HashMap<>();
        mEditTextMap = new HashMap<>();
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
        mCheckBoxes = new ArrayList<>();
        for (final String title : titles) {
            final CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(title);
            checkBox.setGravity(Gravity.CENTER);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        MaterialEditText editText = new MaterialEditText(getContext());
                        editText.setHint(title + "匹配 (正则式, 可选)");
                        editText.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NORMAL);
                        mViewGroup.addView(editText);
                        mEditTextMap.put(title, editText);
                    } else {
                        mViewGroup.removeView(mEditTextMap.get(title));
                        mEditTextMap.remove(title);
                    }
                }
            });
            mCheckBoxes.add(checkBox);
            mViewGroup.addView(checkBox);
        }
    }

    public interface TableSettingCallBack {
        void onFinish(Map<String, Integer> indexMap, Map<String, String> reMap);
    }
}
