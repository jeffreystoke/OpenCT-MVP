package cc.metapro.openct.customviews;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;

public class ClickDialog extends DialogFragment implements View.OnClickListener {

    public static final String NEED_CLICK = "need_click";
    public static final String NEED_VAlUE = "need_value";
    public static final String NEED_INPUT = "need_input";

    private static String type;

    @BindView(R.id.username)
    RadioButton mRadioUsername;
    @BindView(R.id.password)
    RadioButton mRadioPassword;
    @BindView(R.id.captcha)
    RadioButton mRadioCaptcha;
    @BindView(R.id.login)
    RadioButton mRadioLogin;
    @BindView(R.id.grade_table)
    RadioButton mRadioGrade;
    @BindView(R.id.class_table)
    RadioButton mRadioClass;
    @BindView(R.id.search)
    RadioButton mRadioSearch;
    @BindView(R.id.borrow)
    RadioButton mRadioBorrow;
    @BindView(R.id.checkbox)
    RadioButton mRadioSelect;
    @BindView(R.id.page)
    RadioButton mRadioPage;
    @BindView(R.id.value)
    MaterialEditText mValue;
    @BindView(R.id.cancel)
    TextView mCancel;
    @BindView(R.id.ok)
    TextView mConfirm;

    private static CallBack mCallBack;

    private static String key;

    public static ClickDialog newInstance(CallBack callBack, String id) {
         mCallBack = callBack;
        key = id;
        return new ClickDialog();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        dismiss();
    }

    @OnClick(R.id.ok)
    public void ok() {
        Map<String ,String> map = new HashMap<>(1);
        map.put(type, generateValue(type));
        mCallBack.setResult(map);
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_click, container);

        ButterKnife.bind(this, view);

        mRadioCaptcha.setOnClickListener(this);
        mRadioUsername.setOnClickListener(this);
        mRadioPassword.setOnClickListener(this);
        mRadioLogin.setOnClickListener(this);
        mRadioBorrow.setOnClickListener(this);
        mRadioSelect.setOnClickListener(this);
        mRadioPage.setOnClickListener(this);
        mRadioSearch.setOnClickListener(this);
        mRadioClass.setOnClickListener(this);
        mRadioGrade.setOnClickListener(this);
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
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.username:
            case R.id.password:
                mValue.setVisibility(View.VISIBLE);
                type = NEED_VAlUE;
                break;
            case R.id.captcha:
                type = NEED_INPUT;
                mValue.setVisibility(View.VISIBLE);
                break;
            default:
                type = NEED_CLICK;
                mValue.setVisibility(View.GONE);
        }
    }

    private String generateValue(String type) {
        if (NEED_CLICK.equals(type)) {
            String fun = "javascript:var node = document.getElementById(\""+ key +"\");" +
                    "if(!node){node=document.getElementsByName(\""+ key + "\")[0];}" +
                    "node.click();";
            return fun;
        } else if (NEED_INPUT.equals(type) || NEED_VAlUE.equals(type)) {
            String fun = "javascript:var count=1;var node=document.getElementById(\""+ key +"\");" +
                    "if(!node){node=document.getElementsByName(\""+ key + "\")[0];}" +
                    "node.setAttribute(\"value\", \""+ mValue.getText() +"\")";
            return fun;
        }
        return null;
    }

    public interface CallBack {

        void setResult(Map<String, String> map);

    }
}
