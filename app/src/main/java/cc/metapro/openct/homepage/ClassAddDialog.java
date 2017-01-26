package cc.metapro.openct.homepage;

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

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.REHelper;

@Keep
public class ClassAddDialog extends DialogFragment {

    private static ClassAddCallBack mCallBack;
    private static String id;
    private static String mTitle;
    private static ClassInfo mClassInfo;
    private static int mDayOfWeek;
    private static int mColor;
    @BindView(R.id.title)
    TextView mTitleView;
    @BindView(R.id.class_name)
    MaterialEditText mName;
    @BindView(R.id.class_type)
    MaterialEditText mType;
    @BindView(R.id.odd)
    RadioButton mOddRadio;
    @BindView(R.id.even)
    RadioButton mEvenRadio;
    @BindView(R.id.class_teacher)
    MaterialEditText mTeacher;
    @BindView(R.id.class_place)
    MaterialEditText mClassPlace;
    @BindView(R.id.week)
    MaterialEditText mWeekDay;
    @BindView(R.id.time_start)
    MaterialEditText mTimeStart;
    @BindView(R.id.time_end)
    MaterialEditText mTimeEnd;
    @BindView(R.id.week_start)
    MaterialEditText mWeekStart;
    @BindView(R.id.week_end)
    MaterialEditText mWeekEnd;
    @BindView(R.id.color_spinner)
    Spinner mColorSpinner;

    public static ClassAddDialog newInstance(String title, @Nullable EnrichedClassInfo info, ClassAddCallBack callBack) {
        mCallBack = callBack;
        mTitle = title;
        if (info != null) {
            mClassInfo = info.getFirstClassInfo();
            mDayOfWeek = info.getDayOfWeek();
            mColor = info.getColor();
        }
        return new ClassAddDialog();
    }

    @OnClick(R.id.ok)
    public void confirm() {
        String name = mName.getText().toString();
        String during = mWeekStart.getText().toString() + " - " + mWeekEnd.getText().toString();
        String time = mTimeStart.getText().toString() + " - " + mTimeEnd.getText().toString() + " 节";
        if (!REHelper.isEmpty(name) && !REHelper.isEmpty(during) && !REHelper.isEmpty(time)) {
            String type = mType.getText().toString();
            String teacher = mTeacher.getText().toString();
            String place = mClassPlace.getText().toString();
            ClassInfo info = new ClassInfo(id, name, type, time, during, teacher, place, mOddRadio.isChecked(), mEvenRadio.isChecked());
            int dayOfWeek = Integer.parseInt(mWeekDay.getText().toString());
            int color = Constants.getColor(mColorSpinner.getSelectedItemPosition());
            int dailySeq = Integer.parseInt(mTimeStart.getText().toString());
            EnrichedClassInfo classInfo = new EnrichedClassInfo(info, dayOfWeek, dailySeq, color);
            mCallBack.onAdded(classInfo);
            Toast.makeText(getActivity(), mTitle + "成功", Toast.LENGTH_LONG).show();
            dismiss();
        } else {
            Toast.makeText(getActivity(), "请输入课程名称, 上课时间, 课程周期\n(这些都很重要)", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_class_add, container);
        ButterKnife.bind(this, view);
        mTitleView.setText(mTitle);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, Constants.colorString);
        mColorSpinner.setAdapter(adapter);
        mColorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mColorSpinner.setBackgroundColor(Constants.getColor(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (mClassInfo != null) {
            id = mClassInfo.getId();
            mName.setText(mClassInfo.getName());
            mType.setText(mClassInfo.getType());
            mTeacher.setText(mClassInfo.getTeacher());

            mWeekDay.setText(mDayOfWeek + "");
            int[] timeStartEnd = REHelper.getStartEnd(mClassInfo.getTime());
            mTimeStart.setText(timeStartEnd[0] + "");
            mTimeEnd.setText(timeStartEnd[1] + "");

            int[] duringStartEnd = REHelper.getStartEnd(mClassInfo.getDuring());
            mWeekStart.setText(duringStartEnd[0] + "");
            mWeekEnd.setText(duringStartEnd[1] + "");

            mClassPlace.setText(mClassInfo.getPlace());
            int i = 0;
            for (String s : Constants.colorString) {
                if (Color.parseColor(s) == mColor) {
                    mColorSpinner.setSelection(i, true);
                    break;
                }
                i++;
            }

            if (mClassInfo.isEvenWeek()) {
                mEvenRadio.setChecked(true);
            } else if (mClassInfo.isOddWeek()) {
                mOddRadio.setChecked(true);
            }
        }
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallBack = null;
    }

    public interface ClassAddCallBack {
        void onAdded(EnrichedClassInfo classInfo);
    }
}
