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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.LoginPresenter;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormUtils;

public class FormDialog extends DialogFragment {

    private static Form mForm;
    private static LoginPresenter mPresenter;

    @BindView(R.id.form_content_layout)
    LinearLayout mBaseLinearLayout;

    public static FormDialog newInstance(Form form, LoginPresenter presenter) {
        mForm = form;
        mPresenter = presenter;
        return new FormDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = FormUtils.getFormView(getContext(), null, mForm);
        ButterKnife.bind(this, view);
        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("选择查询详情")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
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
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_Dialog_MinWidth);
    }
}
