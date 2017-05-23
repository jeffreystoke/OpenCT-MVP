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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;

public class TableSettingDialog extends BaseDialog {

    public static List<String> mOptions;
    private static List<String> mContents;
    private static int mIndex;
    private static List<String> mShowingOptions;

    private static TableSettingCallBack mCallBack;
    private static Map<String, Integer> mResult;

    public static TableSettingDialog newInstance(List<Element> rawInfoList, TableSettingCallBack callBack) {
        Element element = null;
        for (Element td : rawInfoList) {
            if (td.text().length() > 10) {
                element = td;
                break;
            }
        }

        if (element != null) {
            String[] defaultTitles = {
                    Constants.NAME, Constants.TIME, Constants.DURING,
                    Constants.TYPE, Constants.PLACE, Constants.TEACHER
            };
            mOptions = new ArrayList<>(Arrays.asList(defaultTitles));
            mShowingOptions = mOptions;
            mIndex = 0;
            String sample = element.text().split(HTMLUtils.BR_REPLACER + HTMLUtils.BR_REPLACER + "+")[0];
            String[] s = sample.split(HTMLUtils.BR_REPLACER);
            mResult = new HashMap<>();
            mContents = new ArrayList<>(Arrays.asList(s));
            mCallBack = callBack;
        } else {
            throw new NullPointerException("Can't find sample element");
        }

        return new TableSettingDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.what_is, mContents.get(mIndex++)))
                .setMultiChoiceItems(mShowingOptions.toArray(new String[0]), null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which, boolean isChecked) {
                        String s = mShowingOptions.get(which);
                        if (isChecked) {
                            mResult.put(s, mIndex - 1);
                        } else {
                            mResult.remove(s);
                        }
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog1, int which) {
                        mShowingOptions = new ArrayList<>();
                        for (String s : mOptions) {
                            if (!mResult.containsKey(s)) {
                                mShowingOptions.add(s);
                            }
                        }

                        if (mShowingOptions.size() > 0 && mIndex < mContents.size()) {
                            new TableSettingDialog().show(getFragmentManager(), "table_setting");
                        } else {
                            mCallBack.onResult(mResult);

                            mContents = null;
                            mOptions = null;
                            mCallBack = null;
                            mResult = null;
                        }
                    }
                })
                .create();
    }

    interface TableSettingCallBack {
        void onResult(Map<String, Integer> indexMap);
    }
}
