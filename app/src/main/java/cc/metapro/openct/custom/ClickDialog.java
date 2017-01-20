package cc.metapro.openct.custom;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;

public class ClickDialog extends DialogFragment implements View.OnClickListener {

    // 用户输入用户名密码, 回放时将直接填写
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";

    // 用户输入验证码, 在回放时执行到此项应出现对话框输入验证码
    public static final String CAPTCHA = "captcha";

    // 点击登录, 若有此项将会区分登录前登陆后操作
    public static final String SUBMIT_BUTTON = "login_button";

    // 输入框, 需要弹出窗口
    public static final String INPUT = "input";

    // 点击了链接
    public static final String LINK = "link";
    private static CallBack mCallBack;
    private static String mId;
    @BindView(R.id.captcha)
    RadioButton mRadioCaptcha;
    @BindView(R.id.submit)
    RadioButton mRadioLogin;
    @BindView(R.id.value)
    MaterialEditText mValue;
    private String mType = LINK;
    private Map<String, String> userPass;

    public static ClickDialog newInstance(CallBack callBack, String id) {
        mCallBack = callBack;
        mId = id;
        return new ClickDialog();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        dismiss();
    }

    @OnClick(R.id.ok)
    public void ok() {
        if (CAPTCHA.equals(mType) || INPUT.equals(mType)) {
            String hint = "";
            if (CAPTCHA.equals(mType)) {
                hint = "请输入验证码";
            } else if (INPUT.equals(mType)) {
                hint = "请输入内容";
            }
            InputDialog.newInstance(hint, new InputDialog.InputCallBack() {
                @Override
                public void onConfirm(String result) {
                    mCallBack.setResult(mType, generateValue(mType), result);
                }
            }).show(getFragmentManager(), "input_dialog");
        } else {
            mCallBack.setResult(mType, generateValue(mType), null);
        }
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_click, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
        switch (CustomActivity.CUSTOM_TYPE) {
            case CustomActivity.CMS_CLASS:
            case CustomActivity.CMS_GRADE:
                userPass = Loader.getCmsStuInfo(getActivity());
                break;
            case CustomActivity.LIB_BORROW:
            case CustomActivity.LIB_SEARCH:
                userPass = Loader.getLibStuInfo(getActivity());
                break;
        }
    }

    @Override
    @OnClick({R.id.username, R.id.password, R.id.captcha, R.id.submit, R.id.input, R.id.link})
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.username:
                mValue.setVisibility(View.VISIBLE);
                mValue.setText(userPass.get(Constants.USERNAME_KEY));
                mType = USERNAME;
                break;
            case R.id.password:
                mValue.setVisibility(View.VISIBLE);
                mValue.setText(userPass.get(Constants.PASSWORD_KEY));
                mType = PASSWORD;
                break;
            case R.id.captcha:
                // 选择了验证码, 点击确定后弹出窗口输入验证码
                mValue.setVisibility(View.GONE);
                mType = CAPTCHA;
                break;
            case R.id.input:
                // 选择了输入框, 点击确定后弹出窗口输入内容
                mValue.setVisibility(View.GONE);
                mType = INPUT;
                break;
            case R.id.submit:
                mValue.setVisibility(View.GONE);
                mType = SUBMIT_BUTTON;
                break;
            case R.id.link:
                mValue.setVisibility(View.GONE);
                mType = LINK;
                break;
        }
    }

    private String generateValue(String type) {
        if (USERNAME.equals(type) || PASSWORD.equals(type)) {
            // 正方 需要一个 count 变量才能输入验证码
            return "var count=1;var openCTNode=document.getElementById(\"" + mId + "\");" +
                    "if(!openCTNode){openCTNode=document.getElementsByName(\"" + mId + "\")[0];}" +
                    "openCTNode.setAttribute(\"value\",\"" + mValue.getText() + "\");";
        } else if (CAPTCHA.equals(type)) {
            // 准备填写验证码的JS代码, 调用时需要加上 验证码 和 ";"
            return "var openCTCaptchaText=document.getElementById(\"" + mId + "\");" +
                    "if(!openCTCaptchaText){openCTCaptchaText=document.getElementsByName(\"" + mId + "\")[0];}" +
                    "openCTCaptchaText.setAttribute(\"value\",";
        } else if (SUBMIT_BUTTON.equals(type)) {
            return "var openCTButton=document.getElementById(\"" + mId + "\");" +
                    "if(!openCTButton){openCTButton=document.getElementsByName(\"" + mId + "\")[0];}" +
                    "if(openCTButton){openCTButton.click();}";
        } else if (INPUT.equals(type)) {
            // 需要填写内容的JS代码, 调用时需要加上 \" + yourValue + "\");"
            return "var openCTInputText=document.getElementById(\"" + mId + "\");" +
                    "if(!openCTInputText){captchaText=document.getElementsByName(\"" + mId + "\")[0];}" +
                    "openCTInputText.setAttribute(\"value\",";
        } else if (LINK.equals(type)) {
            // 链接, 检查当前页面中 href
//            return mId.substring(mId.lastIndexOf("/"));
            return "var openCTLinks=document.getElementsByTagName(\"a\");" +
                    "for(var i=0;i<openCTLinks.length;i++){" +
                    "if(openCTLinks[i].href.indexOf(\"" + mId.substring(mId.lastIndexOf("/") + 1) + "\")>=0){" +
                    "openCTLinks[i].click();}}";
        }
        return null;
    }

    public interface CallBack {

        void setResult(String key, String cmd, String value);

    }
}
