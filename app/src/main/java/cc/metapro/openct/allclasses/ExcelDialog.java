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

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;

import cc.metapro.openct.R;
import cc.metapro.openct.utils.ExcelHelper;
import cc.metapro.openct.utils.FileUtils;
import cc.metapro.openct.utils.base.BaseDialog;


public class ExcelDialog extends BaseDialog {

    private static final int FILE_SELECT_CODE = 101;
    private static final int REQUEST_EXTERNAL_FILE_WRITE = 102;

    private static ExcelCallback mCallback;

    public static ExcelDialog newInstance(@NonNull ExcelCallback callback) {
        mCallback = callback;
        return new ExcelDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.import_from_excel)
                .setMessage(R.string.select_file_tip)
                .setPositiveButton(R.string.select_file, null)
                .setNegativeButton(android.R.string.cancel, null)
                .setNeutralButton(R.string.refer_to_usage, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse("https://github.com/jeffreystoke/openct-mvp#import-from-xlsx-excel-2007"));
                        startActivity(intent);
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            int hasPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_FILE_WRITE);
                            } else {
                                showFilerChooser();
                            }
                        } else {
                            showFilerChooser();
                        }
                    }
                });
            }
        });
        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCallback = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_SELECT_CODE && resultCode == AppCompatActivity.RESULT_OK) {
            Uri uri = data.getData();
            String path = null;
            path = FileUtils.getPath(getActivity(), uri);
            if (path != null) {
                File file = new File(path);
                Log.d("FileChooser", file.getAbsolutePath());
                try {
                    String table = ExcelHelper.xlsxToTable(path);
                    mCallback.onJsonResult(ExcelHelper.tableToJson(table));
                    dismiss();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_FILE_WRITE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showFilerChooser();
            } else {
                dismiss();
                Toast.makeText(getActivity(), R.string.no_write_permission, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showFilerChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_schedule_file)), FILE_SELECT_CODE);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.fail_file_chooser, Toast.LENGTH_LONG).show();
        }
    }

    interface ExcelCallback {
        void onJsonResult(String json);
    }

}
