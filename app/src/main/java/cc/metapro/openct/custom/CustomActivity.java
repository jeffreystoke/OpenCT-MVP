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

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.webview.JSInteraction;
import cc.metapro.openct.custom.webview.SchoolWebViewClient;
import cc.metapro.openct.customviews.ClickDialog;

public class CustomActivity extends AppCompatActivity {

    public static final String TAG = "CUSTOM";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.custom_web_view)
    WebView mWebView;
    @BindView(R.id.url_input)
    EditText mURL;
    @BindView(R.id.fab)
    FloatingActionButton mActionButton;
    @BindView(R.id.tips)
    TextView tipText;

    private List<Map<String, String>> nameOrId;

    @OnClick(R.id.fab)
    public void start() {
        String url = getUrl(mURL.getText().toString());
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

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
        nameOrId = new ArrayList<>();
        mWebView.requestFocus();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setWebViewClient(new SchoolWebViewClient());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.addJavascriptInterface(new JSInteraction(new JSInteraction.CallBack() {
            @Override
            public void onClick(String id) {
                ClickDialog dialog = ClickDialog.newInstance(new ClickDialog.CallBack() {
                    @Override
                    public void setResult(final Map<String, String> map) {
                        for (String s : map.keySet()) {
                            if (!ClickDialog.NEED_CLICK.equals(s)) {
                                String fun = map.get(s);
                                mWebView.loadUrl(fun);
                            }
                        }
                        nameOrId.add(map);
                    }
                }, id);
                dialog.show(getSupportFragmentManager(), "click_dialog");
            }
        }), JSInteraction.JSInterface);
    }

    private String getUrl(String input) {
        Pattern pattern = Pattern.compile("^https?://");
        if (!pattern.matcher(input).find()) {
            input = "http://" + input;
        }
        return input;
    }
}
