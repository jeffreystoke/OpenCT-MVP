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
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.HTMLUtils.Form;
import cc.metapro.openct.utils.HTMLUtils.FormUtils;

public class FormDialog extends DialogFragment {

    private static Form mForm;
    private static LoginPresenter mPresenter;
    private LinearLayout mBaseLinearLayout;

    public static FormDialog newInstance(Form form, LoginPresenter presenter) {
        mForm = form;
        mPresenter = presenter;
        return new FormDialog();
    }

    @OnClick(R.id.cancel)
    public void cancel() {
        dismiss();
    }

    @OnClick(R.id.ok)
    public void confirm() {
        Map<String, String> map = new LinkedHashMap<>();
        int j = 0;
        for (int i = 0; i < mForm.size(); i++) {
            Element target = mForm.getItemByIndex(i);
            String tagName = target.tagName();
            if ("select".equalsIgnoreCase(tagName)) {
                Spinner spinner = (Spinner) mBaseLinearLayout.getChildAt(j++);
                Elements elements = target.select("option");
                map.put(target.attr("name"), elements.get(spinner.getSelectedItemPosition()).attr("value"));
            } else if ("input".equalsIgnoreCase(tagName)) {
                if ("text".equalsIgnoreCase(target.attr("type"))) {
                    MaterialEditText editText = (MaterialEditText) mBaseLinearLayout.getChildAt(j++);
                    map.put(target.attr("name"), editText.getText().toString());
                } else if (Pattern.compile(FormUtils.INVISIBLE_FORM_ITEM_PATTERN).matcher(target.toString()).find()) {
                    map.put(target.attr("name"), target.attr("value"));
                }
            } else {
                map.put(target.attr("name"), target.attr("value"));
            }
        }
        mPresenter.loadQuery(getFragmentManager(), mForm.getAction(), map);
        dismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = FormUtils.getFormView(getContext(), container, mForm);
        ButterKnife.bind(this, view);
        mBaseLinearLayout = (LinearLayout) view.findViewById(R.id.form_content_layout);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }
}
