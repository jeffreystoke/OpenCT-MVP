package cc.metapro.openct.custom;

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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.webview.JSInteraction;
import cc.metapro.openct.custom.webview.SchoolWebViewClient;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;

public class CustomActivity extends AppCompatActivity {

    public static final String TAG = "CUSTOM";

    public static final int CMS_CLASS = 1;
    public static final int CMS_GRADE = 2;
    public static final int LIB_SEARCH = 3;
    public static final int LIB_BORROW = 4;

    public static int CUSTOM_TYPE = CMS_CLASS;
    public static CmsFactory.ClassTableInfo classTableInfo;
    public static CustomConfiguration webScriptConfig;
    public static String cmsClassURL;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.custom_web_view)
    WebView mWebView;
    @BindView(R.id.url_input)
    EditText mURL;
    @BindView(R.id.tips)
    TextView tipText;
    @BindView(R.id.web_layout)
    ViewGroup mViewGroup;

    private SchoolWebViewClient mClient;

    @OnClick(R.id.fab)
    public void start() {
        SchoolWebViewClient.replayMode = false;
        String url = getUrl(mURL.getText().toString());
        tipText.setVisibility(View.GONE);
        cmsClassURL = url;
        mWebView.setVisibility(View.VISIBLE);
        webScriptConfig = new CustomConfiguration();
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab_replay)
    public void replay() {
        if (!webScriptConfig.isEmpty()) {
            tipText.setVisibility(View.GONE);
            mWebView.setVisibility(View.VISIBLE);
            mWebView.loadUrl(getUrl(mURL.getText().toString()));
            mClient.performActions(webScriptConfig, getSupportFragmentManager(), mWebView);
        } else {
            Toast.makeText(this, "还没有自定义过, 请点击开始自定义", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.fab_ok)
    public void showTableChooseDialog() {
        mWebView.loadUrl(
                "javascript:var frs=document.getElementsByTagName(\"iframe\");" +
                        "var frameContent=\"\";" +
                        "for(var i=0;i<frs.length;i++){frameContent=frameContent+frs[i].contentDocument.body.innerHTML;}" +
                        "window." + JSInteraction.JSInterface + ".getRaw(" +
                        "document.getElementsByTagName('html')[0].innerHTML + frameContent);"
        );
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

        DBManger manger = DBManger.getInstance(this);
        AdvancedCustomInfo customInfo = manger.getAdvancedCustomInfo();
        if (customInfo != null) {
            cmsClassURL = customInfo.mCmsClassURL;
            if (!TextUtils.isEmpty(cmsClassURL)) {
                mURL.setText(cmsClassURL);
            }
            webScriptConfig = customInfo.mWebScriptConfiguration;
            if (webScriptConfig == null) {
                webScriptConfig = new CustomConfiguration();
            }
            classTableInfo = customInfo.mClassTableInfo;
            if (classTableInfo == null) {
                classTableInfo = new CmsFactory.ClassTableInfo();
            }
        } else {
            webScriptConfig = new CustomConfiguration();
            classTableInfo = new CmsFactory.ClassTableInfo();
        }
        setWebView();
    }

    private void setWebView() {
        mClient = new SchoolWebViewClient();
        mWebView.requestFocus();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setSupportZoom(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.setWebViewClient(mClient);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        mWebView.addJavascriptInterface(new JSInteraction(
                new JSInteraction.ClickCallBack() {
                    @Override
                    public void onClick(String id) {
                        ClickDialog dialog = ClickDialog.newInstance(new ClickDialog.CallBack() {
                            @Override
                            public void setResult(String key, String cmd, String value) {
                                webScriptConfig.addAction(key, cmd);
                                String command = cmd;
                                command += TextUtils.isEmpty(value) ? "" : "\"" + value + "\");";
                                mWebView.loadUrl("javascript:" + command);
                            }
                        }, id);
                        dialog.show(getSupportFragmentManager(), "click_dialog");
                    }
                },
                new JSInteraction.RawCallBack() {
                    @Override
                    public void onLoadRaw(String html) {
                        html = html.replaceAll(Constants.BR, Constants.BR_REPLACER);
                        Document document = Jsoup.parse(html);
                        Elements tables = document.select("table");
                        Map<String, Element> tableMap = new HashMap<>();
                        for (Element element : tables) {
                            String id = element.id();
                            if (TextUtils.isEmpty(id)) {
                                id = element.className();
                            }
                            tableMap.put(id, element);
                        }
                        if (SchoolWebViewClient.replayMode && !TextUtils.isEmpty(classTableInfo.mClassTableID)) {
                            List<Element> list = TableChooseDialog.getClasses(tableMap.get(classTableInfo.mClassTableID));
                            List<EnrichedClassInfo> enrichedClasses = TableChooseDialog.generateClasses(list, classTableInfo, getResources().getDisplayMetrics());
                            DBManger manger = DBManger.getInstance(CustomActivity.this);
                            manger.updateClasses(enrichedClasses);
                            Toast.makeText(CustomActivity.this, "获取课表成功, 请回到主界面查看课表", Toast.LENGTH_LONG).show();
                        } else {
                            TableChooseDialog tableDialog = TableChooseDialog.newInstance(tableMap);
                            tableDialog.show(getSupportFragmentManager(), "table_choose");
                        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewGroup.removeAllViews();
        mWebView.destroy();
    }
}
