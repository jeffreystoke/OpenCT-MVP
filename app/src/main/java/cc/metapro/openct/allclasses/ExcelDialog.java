package cc.metapro.openct.allclasses;

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
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ExcelHelper;


public class ExcelDialog extends DialogFragment {

    private static ExcelCallback mCallback;
    @BindView(R.id.excel_content)
    MaterialEditText mEditText;

    public static ExcelDialog newInstance(@NonNull ExcelCallback callback) {
        mCallback = callback;
        return new ExcelDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_excel, null, false);
        ButterKnife.bind(this, view);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.import_from_excel)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        String text = mEditText.getText().toString();
                        try {
                            String json = ExcelHelper.tableToJson(text);
                            mCallback.onJsonResult(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    interface ExcelCallback {
        void onJsonResult(String json);
    }

}
