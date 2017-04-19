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

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.base.BaseDialog;


public class CETResultDialog extends BaseDialog {

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_cet_result, null);
        ButterKnife.bind(this, view);
        setInfo();
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(android.R.string.ok, null)
                .create();
    }

    private void setInfo() {
        String fullName = result.get(getString(R.string.key_full_name));
        if (!TextUtils.isEmpty(fullName)) {
            mFullName.setVisibility(View.VISIBLE);
            mFullName.setText(getString(R.string.full_name) + ":\t" + fullName);
        }

        String school = result.get(getString(R.string.key_school));
        if (!TextUtils.isEmpty(school)) {
            mSchool.setVisibility(View.VISIBLE);
            mSchool.setText(getString(R.string.school) + ":\t" + school);
        }

        String type = result.get(getString(R.string.key_cet_type));
        if (!TextUtils.isEmpty(type)) {
            mType.setVisibility(View.VISIBLE);
            mType.setText(getString(R.string.cet_type) + ":\t" + type);
        }

        String ticketNum = result.get(getString(R.string.key_ticket_num));
        if (!TextUtils.isEmpty(ticketNum)) {
            mTicketNum.setVisibility(View.VISIBLE);
            mTicketNum.setText(getString(R.string.ticket_number) + ":\t" + ticketNum);
        }

        String time = result.get(getString(R.string.key_cet_time));
        if (!TextUtils.isEmpty(time)) {
            mTime.setVisibility(View.VISIBLE);
            mTime.setText(getString(R.string.exam_time) + time);
        }

        String grade = result.get(getString(R.string.key_cet_grade));
        if (!TextUtils.isEmpty(grade)) {
            mGrade.setVisibility(View.VISIBLE);
            mGrade.setText(getString(R.string.grades) + ":\t" + grade);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        result = null;
    }
}
