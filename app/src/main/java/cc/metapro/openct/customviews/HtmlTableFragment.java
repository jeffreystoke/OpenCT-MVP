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

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;


public class HtmlTableFragment extends Fragment {

    private static final String KEY_HTML = "html";
    @BindView(R.id.table_content)
    TextView mTableContent;

    static HtmlTableFragment newInstance(String html) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_HTML, html);

        HtmlTableFragment fragment = new HtmlTableFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_class_table, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onResume() {
        super.onResume();
        String resource = getArguments().getString(KEY_HTML);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mTableContent.setText(Html.fromHtml(resource, Html.FROM_HTML_MODE_COMPACT));
        } else {
            mTableContent.setText(Html.fromHtml(resource));
        }
    }
}
