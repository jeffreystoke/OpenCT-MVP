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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.openct.R;

@Keep
public class ClickDialog extends DialogFragment {
    private static TypeCallback mTypeCallback;

    public static ClickDialog newInstance(TypeCallback typeCallback) {
        mTypeCallback = typeCallback;
        return new ClickDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.question)
                .setMessage(R.string.what_did_you_clicked)
                .setPositiveButton(R.string.common_input, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTypeCallback.onResult(InteractiveWebView.COMMON_INPUT_FLAG);
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.username_input, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTypeCallback.onResult(InteractiveWebView.USERNAME_INPUT_FLAG);
                        dismiss();
                    }
                })
                .create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    public interface TypeCallback {
        void onResult(String type);
    }
}
