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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
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

    public static LinkSelectionDialog newInstance(String type, Elements elements, LoginPresenter presenter) {
        TYPE = type;
        mLinks = elements;
        mPresenter = presenter;
        return new LinkSelectionDialog();
    }

    @OnClick(R.id.ok)
    public void confirm() {
        for (int i = 0; i < mRadioButtons.size(); i++) {
            if (mRadioButtons.get(i).isChecked()) {
                Element target = mLinks.get(i);
                DBManger manger = DBManger.getInstance(getActivity());
                AdvancedCustomInfo info = manger.getAdvancedCustomInfo(getActivity());
                if (info == null) {
                    info = new AdvancedCustomInfo(getActivity());
                }
                if (CLASS_URL_DIALOG.equals(TYPE)) {
                    info.CLASS_URL_PATTERN = target.html();
                } else if (GRADE_URL_DIALOG.equals(TYPE)) {
                    info.GRADE_URL_PATTERN = target.html();
                } else if (BORROW_URL_DIALOG.equals(TYPE)) {
                    info.BORROW_URL_PATTERN = target.html();
                }
                manger.updateAdvancedCustomClassInfo(info);
                mPresenter.loadTargetPage(getFragmentManager(), target.absUrl("href"));
                break;
            }
        }
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_link_selection, container, false);
        ButterKnife.bind(this, view);
        setView();
        return view;
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
