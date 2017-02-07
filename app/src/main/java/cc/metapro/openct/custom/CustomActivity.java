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

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.custom.dialogs.ClickDialog;
import cc.metapro.openct.custom.dialogs.InputDialog;
import cc.metapro.openct.customviews.TableChooseDialog;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.ActivityUtils;

@Keep
public class CustomActivity extends AppCompatActivity {

    public static final String TAG = CustomActivity.class.getSimpleName();

    public static final int CMS_CLASS = 1;
    public static final int CMS_GRADE = 2;
    public static final int LIB_SEARCH = 3;
    public static final int LIB_BORROW = 4;

    public static int CUSTOM_TYPE = CMS_CLASS;

    public static final int COMMON_MODE = 5;
    public static final int RECORD_MODE = 6;
    public static final int REPLAY_MODE = 7;

    public static final int COMMON_INPUT = 100;
    public static final int USERNAME_INPUT = 101;

    public static int MODE = COMMON_MODE;

    private String USERNAME;
    private String PASSWORD;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.custom_web_view)
    InteractiveWebView mWebView;
    @BindView(R.id.url_input)
    EditText mURL;
    @BindView(R.id.tips)
    TextView tipText;
    @BindView(R.id.web_layout)
    ViewGroup mViewGroup;

    private InteractiveWebView.ClickCallback mClickCallback = new InteractiveWebView.ClickCallback() {
        @Override
        public void onClick(@NonNull final Element element) {
            if (HTMLUtils.isPasswordInput(element)) {
                if (!mWebView.setById(element.id(), "value", PASSWORD)) {
                    mWebView.setByName(element.attr("name"), "value", PASSWORD);
                }
            } else if (HTMLUtils.isTextInput(element)) {
                ClickDialog.newInstance(new ClickDialog.TypeCallback() {
                    @Override
                    public void onResult(int type) {
                        switch (type) {
                            case COMMON_INPUT:
                                if (!mWebView.clickElementById(element.id())) {
                                    mWebView.clickElementsByName(element.attr("name"));
                                }
                                break;
                            case USERNAME_INPUT:
                                if (!mWebView.setById(element.id(), "value", USERNAME)) {
                                    mWebView.setByName(element.attr("name"), "value", USERNAME);
                                }
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), "click_dialog");
            }
        }
    };

    @OnClick(R.id.fab_common)
    public void commonStart() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        MODE = COMMON_MODE;

        mWebView.removeUserClickCallback();
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab)
    public void recordStart() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        MODE = RECORD_MODE;

        mWebView.setUserClickCallback(mClickCallback);
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab_replay)
    public void replay() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        MODE = REPLAY_MODE;

        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab_ok)
    public void showTableChooseDialog() {
        Document document = mWebView.getPageDom();
        ActivityUtils.showTableChooseDialog(getSupportFragmentManager(), TableChooseDialog.CLASS_TABLE_DIALOG, document, null);
    }

    @OnClick(R.id.fab_help)
    public void help() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.unable_to_enter_values)
                .setMessage(R.string.can_not_enter_tip)
                .setPositiveButton(R.string.ok, null)
                .show();
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

        Map<String, String> map = null;
        switch (CUSTOM_TYPE) {
            case CMS_CLASS:
            case CMS_GRADE:
                map = Loader.getCmsStuInfo(this);
                if (map.size() < 2) {
                    Toast.makeText(this, R.string.enrich_cms_info, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case LIB_SEARCH:
            case LIB_BORROW:
                map = Loader.getLibStuInfo(this);
                if (map.size() < 2) {
                    Toast.makeText(this, R.string.enrich_lib_info, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
        if (map != null) {
            USERNAME = map.get(getString(R.string.key_username));
            PASSWORD = map.get(getString(R.string.key_password));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewGroup.removeAllViews();
        mWebView.destroy();
    }
}
