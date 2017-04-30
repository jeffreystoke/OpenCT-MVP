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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.StoreHelper;
import cc.metapro.openct.data.university.UniversityFactory;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;
import cc.metapro.openct.utils.base.LoginPresenter;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CaptchaDialog extends BaseDialog {

    private static final String TAG = CaptchaDialog.class.getSimpleName();
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
        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                UniversityFactory.getOneMoreCAPTCHA();
                e.onNext("");
            }
        });

        Observer<String> observer = new MyObserver<String>(TAG) {
            @Override
            public void onNext(String s) {
                setCaptchaImg();
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                Toast.makeText(getContext(), getString(R.string.fetch_captcha_fail) + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    @OnEditorAction(R.id.captcha_edit_text)
    public boolean onEnter(TextView textView, int i, KeyEvent keyEvent) {
        if (i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            String code = mEditText.getText().toString();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(getActivity(), R.string.enter_captcha, Toast.LENGTH_SHORT).show();
            } else {
                mLoginPresenter.loadUserCenter(getFragmentManager(), code);
                dismiss();
            }
            return true;
        }
        return false;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.diaolg_captcha, null);
        ButterKnife.bind(this, view);
        setCaptchaImg();
        AlertDialog.Builder builder = ActivityUtils.getAlertBuilder(getActivity(), R.string.captcha);
        final AlertDialog dialog = ActivityUtils.addViewToAlertDialog(builder, view);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog1) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String code = mEditText.getText().toString();
                        if (TextUtils.isEmpty(code)) {
                            Toast.makeText(getActivity(), R.string.enter_captcha, Toast.LENGTH_SHORT).show();
                        } else {
                            mLoginPresenter.loadUserCenter(getFragmentManager(), code);
                            dismiss();
                        }
                    }
                });
            }
        });

        return dialog;
    }

    private void setCaptchaImg() {
        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
        if (drawable != null) {
            mTextView.setBackground(drawable);
            mTextView.setText("");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mTextView.setText(R.string.press_to_get_captcha);
        mTextView.setBackground(null);
        mEditText.setText("");
        StoreHelper.delFile(Constants.CAPTCHA_FILE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoginPresenter = null;
    }
}
