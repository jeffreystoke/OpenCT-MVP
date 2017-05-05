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
import android.support.v7.app.ActionBar;
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
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseActivity;

public class CustomActivity extends BaseActivity implements CustomContract.View {

    private static final String KEY_TYPE = "type";

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

    private CustomContract.Presenter mPresenter;
    private String mType;

    public static void actionStart(Context context, String type) {
        Intent intent = new Intent(context, CustomActivity.class);
        intent.putExtra(KEY_TYPE, type);
        context.startActivity(intent);
    }

    @OnClick(R.id.fab)
    public void start() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        mPresenter.setWebView(mWebView, getSupportFragmentManager());
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab_target)
    public void showTableChooseDialog() {
        ActivityUtils.showTableChooseDialog(
                getSupportFragmentManager(), mType, mWebView.getPageDom(), null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        LocalHelper.needUpdateUniversity();

        mType = getIntent().getStringExtra(KEY_TYPE);
        new CustomPresenter(this, this, mType);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_custom;
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (mType) {
            case Constants.TYPE_CLASS:
            case Constants.TYPE_GRADE:
                mURL.setText(LocalHelper.getUniversity(this).getCmsURL());
                break;
            case Constants.TYPE_BORROW:
            case Constants.TYPE_SEARCH:
                mURL.setText(LocalHelper.getUniversity(this).getLibURL());
                break;
        }

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
