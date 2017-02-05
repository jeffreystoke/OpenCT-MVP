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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
import cc.metapro.openct.data.university.UniversityFactory;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

@Keep
public class CaptchaDialog extends DialogFragment {

    private static LoginPresenter mLoginPresenter;
    @BindView(R.id.captcha_image)
    TextView mTextView;
    @BindView(R.id.captcha_edit_text)
    MaterialEditText mEditText;

    public static CaptchaDialog newInstance(LoginPresenter presenter) {
        mLoginPresenter = presenter;
        return new CaptchaDialog();
    }

    @OnClick(R.id.captcha_image)
    public void loadCaptcha() {
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                UniversityFactory.getOneMoreCAPTCHA();
                e.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        Toast.makeText(getContext(), "获取验证码失败\n" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                        return "";
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        setCaptchaImg();
                    }
                })
                .subscribe();
    }

    @OnClick(R.id.ok)
    public void go() {
        String code = mEditText.getText().toString();
        if (TextUtils.isEmpty(code)) {
            Toast.makeText(getActivity(), R.string.enter_captcha, Toast.LENGTH_SHORT).show();
        } else {
            mLoginPresenter.loadUserCenter(getFragmentManager(), code);
            dismiss();
        }
    }

    @OnEditorAction(R.id.captcha_edit_text)
    public boolean onEnter(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            go();
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.diaolg_captcha, container);
        ButterKnife.bind(this, view);
        setCaptchaImg();
        return view;
    }

    private void setCaptchaImg() {
        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
        if (drawable != null) {
            mTextView.setBackground(drawable);
            mTextView.setText("");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mTextView.setText(R.string.press_to_get_captcha);
        mTextView.setBackground(null);
        mEditText.setText("");
        StoreHelper.delFile(Constants.CAPTCHA_FILE);
    }
}
