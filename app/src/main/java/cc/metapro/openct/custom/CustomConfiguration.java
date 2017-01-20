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

import android.support.v4.app.FragmentManager;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.data.source.StoreHelper;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class CustomConfiguration {

    private List<String> mTypes;
    private List<String> mCmds;
    private Observer<Integer> mObserver;
    private int mIndex = 0;

    public CustomConfiguration() {
        mTypes = new ArrayList<>();
        mCmds = new ArrayList<>();
    }

    public void addAction(String type, String cmd) {
        mTypes.add(type);
        mCmds.add(cmd);
    }

    private void showInputDialog(FragmentManager manager, final WebView view) {
        String hint = "";
        switch (mTypes.get(mIndex)) {
            case ClickDialog.CAPTCHA:
                hint = "请输入验证码";
                break;
            case ClickDialog.INPUT:
                hint = "请输入内容";
        }
        InputDialog.newInstance(hint, new InputDialog.InputCallBack() {
            @Override
            public void onConfirm(String result) {
                String cmd = "javascript:" + mCmds.get(mIndex++) + "\"" + result + "\");";
                view.loadUrl(cmd);
                mObserver.onNext(0);
            }
        }).show(manager, "input_dialog");
    }

    public Observer<Integer> getCmdExe(final FragmentManager manager, final WebView view) {
        mIndex = 0;
        mObserver = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {
                if (mIndex >= mTypes.size() || mIndex < 0) return;
                String type = mTypes.get(mIndex);
                switch (type) {
                    case ClickDialog.CAPTCHA:
                    case ClickDialog.INPUT:
                        showInputDialog(manager, view);
                        break;
                    default:
                        view.loadUrl("javascript:" + mCmds.get(mIndex++));
                        if (mIndex < mTypes.size() && mIndex > 0) {
                            try {
                                if (!(ClickDialog.LINK.equals(mTypes.get(mIndex - 1)) && ClickDialog.SUBMIT_BUTTON.equals(mTypes.get(mIndex - 1)))) {
                                    onNext(0);
                                }
                            } catch (Exception e) {
                                onNext(0);
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
        return mObserver;
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

    public boolean isEmpty() {
        return mTypes == null || mTypes.size() == 0;
    }
}
