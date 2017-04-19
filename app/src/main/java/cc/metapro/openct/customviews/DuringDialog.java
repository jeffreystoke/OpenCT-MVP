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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;


public class DuringDialog extends BaseDialog {

    private static boolean[] DURING;
    @BindView(R.id.during_container)
    GridLayout mGridLayout;
    private TextView[][] selections;

    public static DuringDialog newInstance(@NonNull boolean[] during) {
        DURING = during;
        if (DURING.length < Constants.WEEKS) {
            DURING = Arrays.copyOf(during, Constants.WEEKS);
        }
        return new DuringDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_during, null, false);
        ButterKnife.bind(this, view);

        setViews(mGridLayout);
        selections = new TextView[5][6];

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.set_during)
                .setView(view).create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    private void setViews(GridLayout gridLayout) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 6; j++) {
                final int week = i * 6 + j + 1;
                final TextView textView = new TextView(getActivity());
                textView.setText(week + gridLayout.getContext().getString(R.string.week));
                textView.setGravity(Gravity.CENTER);
                if (DURING[week]) {
                    textView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.text_view_card_style_blue));
                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
                } else {
                    textView.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.text_view_card_style_grey));
                    textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.material_grey));
                }

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DURING[week] = !DURING[week];
                    }
                });
                GridLayout.Spec row = GridLayout.spec(i, 1, 1);
                GridLayout.Spec col = GridLayout.spec(j, 1, 1);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(row, col);
                gridLayout.addView(textView, params);
                selections[i][j] = textView;
            }
        }
    }
}
