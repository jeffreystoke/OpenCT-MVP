package cc.metapro.openct.customviews;

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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;

@Keep
public class TableSettingDialog extends DialogFragment {

    public static final String NAME = "课程名称";
    public static final String TIME = "上课时间";
    public static final String TYPE = "课程类型";
    public static final String DURING = "课程周期";
    public static final String PLACE = "上课地点";
    public static final String TEACHER = "授课教师";

    public static final String[] titles = {NAME, TIME, TYPE, DURING, PLACE, TEACHER};
    private static String[] mStrings;
    private static TableSettingCallBack mCallBack;
    private final String TAG = TableSettingDialog.class.getSimpleName();

    @BindView(R.id.options_layout)
    ViewGroup mViewGroup;
    private List<CheckBox> mCheckBoxes;
    private int mIndex = 0;

    private Map<String, Integer> mResultIndexMap = new HashMap<>();

    public static TableSettingDialog newInstance(List<Element> rawInfoList, TableSettingCallBack callBack) throws Exception {
        Element element = null;
        for (Element td : rawInfoList) {
            if (td.text().length() > 10) {
                element = td;
                break;
            }
        }
        if (element != null) {
            String sample = element.text().split(HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+")[0];
            mStrings = sample.split(HTMLUtils.BR_REPLACER);
            mCallBack = callBack;
            return new TableSettingDialog();
        } else {
            throw new Exception("");
        }
    }

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
                getDialog().setTitle(mStrings[mIndex]);
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else {
            mCallBack.onResult(mResultIndexMap);
            dismiss();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_table_setting, null);
        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(mStrings[0])
                .setMessage(R.string.what_is_that)
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button button = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirm();
                    }
                });
            }
        });

        addClassTableOptions();
        setCancelable(false);

        return dialog;
    }

    private void addClassTableOptions() {
        mCheckBoxes = new ArrayList<>();
        for (final String title : titles) {
            final CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(title);
            checkBox.setGravity(Gravity.CENTER);
            mCheckBoxes.add(checkBox);
            mViewGroup.addView(checkBox);
        }
    }

    public interface TableSettingCallBack {
        void onResult(Map<String, Integer> indexMap);
    }
}
