package cc.metapro.openct.grades;

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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;

public class CETQueryDialog extends DialogFragment {

    private static GradeContract.Presenter mPresenter;
    SharedPreferences mPreferences;
    @BindView(R.id.ticket_num)
    MaterialEditText mNum;
    @BindView(R.id.full_name)
    MaterialEditText mName;

    static CETQueryDialog newInstance(GradeContract.Presenter presenter) {
        mPresenter = presenter;
        return new CETQueryDialog();
    }

    @OnClick(R.id.ok)
    public void queryCet() {
        Map<String, String> queryMap = new HashMap<>(2);
        queryMap.put(getString(R.string.key_ticket_num), mNum.getText().toString());
        queryMap.put(getString(R.string.key_full_name), mName.getText().toString());
        mPresenter.loadCETGrade(queryMap);
        dismiss();
    }

    @OnClick(R.id.preserve)
    public void save() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(getString(R.string.pref_cet_ticket_num), mNum.getText().toString());
        editor.putString(getString(R.string.pref_cet_full_name), mName.getText().toString());
        editor.apply();
        Toast.makeText(getContext(), "已保存", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_cet_query, container);
        ButterKnife.bind(this, view);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        mNum.setText(mPreferences.getString(getString(R.string.pref_cet_ticket_num), ""));
        mName.setText(mPreferences.getString(getString(R.string.pref_cet_full_name), ""));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }
}
