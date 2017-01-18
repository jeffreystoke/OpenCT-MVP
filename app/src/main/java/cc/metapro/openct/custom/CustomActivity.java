package cc.metapro.openct.custom;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.webview.InJavaScriptLocalObj;
import cc.metapro.openct.data.webview.SchoolWebViewClient;

public class CustomActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.custom_web_view)
    WebView mWebView;

    @BindView(R.id.url_input)
    EditText mURL;

    @BindView(R.id.fab_refresh)
    FloatingActionButton mActionButton;

    @BindView(R.id.tips)
    TextView tipText;

    @BindView(R.id.web_text_layout)
    LinearLayout mLayout;

    @OnClick(R.id.fab_refresh)
    public void start() {
        String url = getUrl(mURL.getText().toString());
        mWebView.loadUrl(url);
        tipText.setVisibility(View.GONE);
        mLayout.setVisibility(View.VISIBLE);
    }

    private static final String JS_OBJ = "local_obj";

    private InJavaScriptLocalObj mLocalObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mLocalObj = new InJavaScriptLocalObj();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(mLocalObj, JS_OBJ);
        mWebView.setWebViewClient(new SchoolWebViewClient());
    }

    private String getUrl(String input) {
        Pattern pattern = Pattern.compile("^https?://");
        if (!pattern.matcher(input).find()){
            input = "http://" + input;
        }
        return input;
    }
}
