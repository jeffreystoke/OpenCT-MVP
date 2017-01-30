package cc.metapro.openct.grades.cet;

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
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;


public class CETResultDialog extends DialogFragment {

    private static Map<String, String> result;
    @BindView(R.id.full_name)
    TextView mFullName;
    @BindView(R.id.school)
    TextView mSchool;
    @BindView(R.id.type)
    TextView mType;
    @BindView(R.id.ticket_num)
    TextView mTicketNum;
    @BindView(R.id.time)
    TextView mTime;
    @BindView(R.id.grade)
    TextView mGrade;

    public static CETResultDialog newInstance(Map<String, String> resultMap) {
        result = resultMap;
        return new CETResultDialog();
    }

    @OnClick(R.id.ok)
    public void confirm() {
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cet_result, container);
        ButterKnife.bind(this, view);

        setInfo();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    private void setInfo() {
        String fullName = result.get(getString(R.string.key_full_name));
        if (!TextUtils.isEmpty(fullName)) {
            mFullName.setVisibility(View.VISIBLE);
            mFullName.setText("姓名: \t" + fullName);
        }

        String school = result.get(getString(R.string.key_school));
        if (!TextUtils.isEmpty(school)) {
            mSchool.setVisibility(View.VISIBLE);
            mSchool.setText("学校: \t" + school);
        }

        String type = result.get(getString(R.string.key_cet_type));
        if (!TextUtils.isEmpty(type)) {
            mType.setVisibility(View.VISIBLE);
            mType.setText("CET类型: \t" + type);
        }

        String ticketNum = result.get(getString(R.string.key_ticket_num));
        if (!TextUtils.isEmpty(ticketNum)) {
            mTicketNum.setVisibility(View.VISIBLE);
            mTicketNum.setText("准考证号: \t" + ticketNum);
        }

        String time = result.get(getString(R.string.key_cet_time));
        if (!TextUtils.isEmpty(time)) {
            mTime.setVisibility(View.VISIBLE);
            mTime.setText("考试时间: \t" + time);
        }

        String grade = result.get(getString(R.string.key_cet_grade));
        if (!TextUtils.isEmpty(grade)) {
            mGrade.setVisibility(View.VISIBLE);
            mGrade.setText("成绩: \t" + grade);
        }
    }
}
