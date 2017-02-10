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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;

@Keep
public class CustomActivity extends AppCompatActivity implements CustomContract.View {

    public static final String TAG = CustomActivity.class.getSimpleName();

    public static String CUSTOM_TYPE = Constants.TYPE_CLASS;

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
    @BindView(R.id.fab_next)
    FloatingActionButton mFabNext;
    private CustomContract.Presenter mPresenter;
    private WebConfiguration mConfiguration;

    public static void actionStart(Context context, String type) {
        CUSTOM_TYPE = type;
        Intent intent = new Intent(context, CustomActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void startRecord() {
        mURL.setText(getUrl());

        mConfiguration = new WebConfiguration();

        mPresenter.setWebView(mWebView, getSupportFragmentManager());

        mFabNext.setVisibility(View.GONE);
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);

        mWebView.loadUrl(getUrl());
    }

    @OnClick(R.id.fab_replay)
    public void replay() {
        mURL.setText(getUrl());

        mWebView.removeUserClickCallback();

        mFabNext.setVisibility(View.VISIBLE);
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mPresenter.execCommands(mWebView);
    }

    @OnClick(R.id.fab_target)
    public void showTableChooseDialog() {
        switch (CUSTOM_TYPE) {
            case Constants.TYPE_CLASS:
                Constants.advCustomInfo.setClassWebConfig(mConfiguration);
                break;
            case Constants.TYPE_GRADE:
                Constants.advCustomInfo.setGradeWebConfig(mConfiguration);
                break;
            case Constants.TYPE_SEARCH:
                break;
            case Constants.TYPE_BORROW:
                Constants.advCustomInfo.setBorrowWebConfig(mConfiguration);
                break;
        }
        ActivityUtils.showTableChooseDialog(getSupportFragmentManager(), CUSTOM_TYPE, mWebView.getPageDom(), null);
    }

    @OnClick(R.id.fab_next)
    public void nextStep() {
        mPresenter.nextStep(mWebView, getSupportFragmentManager());
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

        new CustomPresenter(this, this, CUSTOM_TYPE);
    }

    @Override
    public String getUrl() {
        return URLUtil.guessUrl(mURL.getText().toString());
    }

    @Override
    public void disableNextStep() {
        mFabNext.setEnabled(false);
        mFabNext.setClickable(false);
    }

    @Override
    public void enableNextStep() {
        mFabNext.setEnabled(true);
        mFabNext.setClickable(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewGroup.removeAllViews();
        mWebView.destroy();
    }

    @Override
    public void setPresenter(CustomContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
