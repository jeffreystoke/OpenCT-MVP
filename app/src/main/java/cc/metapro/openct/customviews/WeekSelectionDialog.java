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

import cc.metapro.openct.R;
import cc.metapro.openct.utils.PrefHelper;
import cc.metapro.openct.utils.base.BaseDialog;
import cc.metapro.openct.widget.DailyClassWidget;


public class WeekSelectionDialog extends BaseDialog {

    private static SelectionCallback sCallBack;

    public static WeekSelectionDialog newInstance(@NonNull SelectionCallback callback) {
        sCallBack = callback;
        return new WeekSelectionDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int currentWeek = Integer.parseInt(PrefHelper.getString(getActivity(), R.string.pref_current_week, "1"));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.select_week)
                .setSingleChoiceItems(R.array.pref_week_seq_keys, currentWeek - 1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int selectedWeek = which + 1;
                                PrefHelper.putString(getActivity(), R.string.pref_current_week, selectedWeek + "");
                                sCallBack.onSelection(selectedWeek);
                                DailyClassWidget.update(getActivity());
                                dismiss();
                            }
                        })
                .create();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sCallBack = null;
    }

    public interface SelectionCallback {
        void onSelection(int index);
    }
}
