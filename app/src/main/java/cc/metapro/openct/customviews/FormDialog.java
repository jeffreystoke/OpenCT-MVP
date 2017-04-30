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
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;
import cc.metapro.openct.utils.base.LoginPresenter;
import cc.metapro.openct.utils.webutils.Form;
import cc.metapro.openct.utils.webutils.FormHandler;
import cc.metapro.openct.utils.webutils.FormUtils;

public class FormDialog extends BaseDialog {

    private static Document document;
    private static LoginPresenter mPresenter;
    @BindView(R.id.content)
    LinearLayout mBaseLinearLayout;
    private Form mForm;
    private boolean selectionChanged;

    public static FormDialog newInstance(Document dom, LoginPresenter presenter) {
        document = dom;
        mPresenter = presenter;
        return new FormDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FormHandler handler = new FormHandler(document);
        if (Constants.QZDATASOFT.equalsIgnoreCase(LocalHelper.getUniversity(getActivity()).getCmsSys())) {
            mForm = handler.getForm(1);
            if (mForm == null) {
                mForm = handler.getForm(0);
            }
        } else {
            mForm = handler.getForm(0);
        }
        View view = null;
        try {
            view = FormUtils.getFormView(getContext(), null, mForm);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.can_not_load_prev_target, Toast.LENGTH_LONG).show();
            dismiss();
            return new AlertDialog.Builder(getActivity()).create();
        }
        ButterKnife.bind(this, view);

        AlertDialog.Builder builder = ActivityUtils
                .getAlertBuilder(getActivity(), R.string.query_what)
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
                                int idx = spinner.getSelectedItemPosition();
                                if (idx != 0) {
                                    selectionChanged = true;
                                }
                                map.put(target.attr("name"), elements.get(idx).attr("value"));
                            } else if ("input".equalsIgnoreCase(tagName)) {
                                if ("text".equalsIgnoreCase(target.attr("type"))) {
                                    MaterialEditText editText = (MaterialEditText) mBaseLinearLayout.getChildAt(j++);
                                    String value = editText.getText().toString();
                                    if (!TextUtils.isEmpty(value)) {
                                        selectionChanged = true;
                                    }
                                    map.put(target.attr("name"), value);
                                } else if (Pattern.compile(FormUtils.INVISIBLE_FORM_ITEM_PATTERN).matcher(target.toString()).find()) {
                                    map.put(target.attr("name"), target.attr("value"));
                                }
                            } else {
                                map.put(target.attr("name"), target.attr("value"));
                            }
                        }
                        mPresenter.loadQuery(getFragmentManager(), mForm.getAction(), map, selectionChanged);
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);

        return ActivityUtils.addViewToAlertDialog(builder, view);
    }
}
