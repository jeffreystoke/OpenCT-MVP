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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;

@Keep
public class CaptchaDialog extends DialogFragment {

    private static LoginPresenter mPresenter;
    @BindView(R.id.captcha_image)
    TextView mTextView;
    @BindView(R.id.captcha_edit_text)
    MaterialEditText mEditText;

    public static CaptchaDialog newInstance(LoginPresenter presenter) {
        CaptchaDialog fragment = new CaptchaDialog();
        mPresenter = presenter;
        return fragment;
    }

    @OnClick(R.id.captcha_image)
    public void loadCaptcha() {
        if (mPresenter != null) {
            mPresenter.loadCaptcha(mTextView);
        }
    }

    @OnClick(R.id.ok)
    public void go() {
        String code = mEditText.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(getActivity(), R.string.enter_captcha, Toast.LENGTH_SHORT).show();
        } else {
            dismiss();
            mPresenter.loadOnline(code);
        }
    }

    @OnEditorAction(R.id.captcha_edit_text)
    public boolean onEnter(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            String code = mEditText.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(getActivity(), R.string.enter_captcha, Toast.LENGTH_SHORT).show();
            } else {
                dismiss();
                mPresenter.loadOnline(code);
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diaolg_captcha, container);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @Override
    public void onResume() {
        mPresenter.loadCaptcha(mTextView);
        super.onResume();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mTextView.setText(R.string.press_to_get_captcha);
        mTextView.setBackground(null);
        StoreHelper.delFile(Constants.CAPTCHA_FILE);
    }
}
