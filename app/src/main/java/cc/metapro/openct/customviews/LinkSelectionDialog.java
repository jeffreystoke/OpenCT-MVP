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
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.AdvancedCustomInfo;


public class LinkSelectionDialog extends DialogFragment {

    public static final String CLASS_URL_DIALOG = "class";
    public static final String GRADE_URL_DIALOG = "grade";
    public static final String BORROW_URL_DIALOG = "borrow";

    private static String TYPE;
    private static Elements mLinks;
    private static LoginPresenter mPresenter;
    @BindView(R.id.radio_group)
    RadioGroup mRadioGroup;
    private List<RadioButton> mRadioButtons;

    public static LinkSelectionDialog newInstance(String type, Elements allLinks, LoginPresenter presenter) {
        TYPE = type;
        mLinks = allLinks;
        mPresenter = presenter;
        return new LinkSelectionDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_link_selection, null);
        ButterKnife.bind(this, view);
        setView();
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.select_target_link)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < mRadioButtons.size(); i++) {
                            if (mRadioButtons.get(i).isChecked()) {
                                Element target = mLinks.get(i);
                                AdvancedCustomInfo info = DBManger.getAdvancedCustomInfo(getActivity());
                                if (CLASS_URL_DIALOG.equals(TYPE)) {
                                    info.CLASS_URL_PATTERN = target.html();
                                } else if (GRADE_URL_DIALOG.equals(TYPE)) {
                                    info.GRADE_URL_PATTERN = target.html();
                                } else if (BORROW_URL_DIALOG.equals(TYPE)) {
                                    info.BORROW_URL_PATTERN = target.html();
                                }
                                DBManger.getInstance(getActivity()).updateAdvancedCustomClassInfo(info);
                                mPresenter.loadTargetPage(getFragmentManager(), target.absUrl("href"));
                                break;
                            }
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.not_in_range_above, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO: 17/2/9 显示更多链接
                    }
                })
                .create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }

    private void setView() {
        mRadioButtons = new ArrayList<>(mLinks.size());
        for (Element link : mLinks) {
            RadioButton button = new RadioButton(getContext());
            button.setText(link.text());
            mRadioGroup.addView(button);
            mRadioButtons.add(button);
        }
    }
}
