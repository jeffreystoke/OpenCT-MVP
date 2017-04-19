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

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.grades.GradeContract;
import cc.metapro.openct.utils.base.BaseDialog;

public class CETQueryDialog extends BaseDialog {

    private static GradeContract.Presenter mPresenter;
    SharedPreferences mPreferences;
    @BindView(R.id.ticket_num)
    MaterialEditText mNum;
    @BindView(R.id.full_name)
    MaterialEditText mName;

    public static CETQueryDialog newInstance(GradeContract.Presenter presenter) {
        mPresenter = presenter;
        return new CETQueryDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_cet_query, null);
        ButterKnife.bind(this, view);
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.cet_query)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Map<String, String> queryMap = new HashMap<>(2);
                        queryMap.put(getString(R.string.key_ticket_num), mNum.getText().toString());
                        queryMap.put(getString(R.string.key_full_name), mName.getText().toString());
                        mPresenter.loadCETGrade(queryMap);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.preserve, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = mPreferences.edit();
                        editor.putString(getString(R.string.pref_cet_ticket_num), mNum.getText().toString());
                        editor.putString(getString(R.string.pref_cet_full_name), mName.getText().toString());
                        editor.apply();
                        Toast.makeText(getContext(), R.string.saved, Toast.LENGTH_SHORT).show();
                    }
                }).create();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mNum.setText(mPreferences.getString(getString(R.string.pref_cet_ticket_num), ""));
        mName.setText(mPreferences.getString(getString(R.string.pref_cet_full_name), ""));
        return alertDialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter = null;
    }
}
