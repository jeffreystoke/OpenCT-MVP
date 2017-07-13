package cc.metapro.openct.custom

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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cc.metapro.interactiveweb.InteractiveWebView
import cc.metapro.openct.R
import cc.metapro.openct.data.source.local.LocalHelper
import cc.metapro.openct.utils.ActivityUtils
import cc.metapro.openct.utils.Constants
import cc.metapro.openct.utils.base.BaseActivity
import cc.metapro.openct.utils.base.BasePresenter

class CustomActivity : BaseActivity(), CustomContract.View {

    @BindView(R.id.toolbar)
    internal lateinit var mToolbar: Toolbar
    @BindView(R.id.custom_web_view)
    internal lateinit var mWebView: InteractiveWebView
    @BindView(R.id.url_input)
    internal lateinit var mURL: EditText
    @BindView(R.id.tips)
    internal lateinit var tipText: TextView
    @BindView(R.id.web_layout)
    internal lateinit var mViewGroup: ViewGroup

    private lateinit var mPresenter: CustomContract.Presenter
    private lateinit var mType: String

    override val presenter: BasePresenter?
        get() = mPresenter

    @OnClick(R.id.fab)
    fun start() {
        val url = URLUtil.guessUrl(mURL.text.toString())
        mURL.setText(url)
        mPresenter.setWebView(mWebView, supportFragmentManager)
        tipText.visibility = View.GONE
        mWebView.visibility = View.VISIBLE
        mWebView.loadUrl(url)
    }

    @OnClick(R.id.fab_target)
    fun showTableChooseDialog() {
        ActivityUtils.showTableChooseDialog(
                supportFragmentManager, mType, mWebView.pageDom, null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        setSupportActionBar(mToolbar)
        val ab = supportActionBar
        ab?.setDisplayHomeAsUpEnabled(true)
        LocalHelper.needUpdateUniversity()

        mType = intent.getStringExtra(KEY_TYPE)
        CustomPresenter(this, this, mType)
    }

    override val layout: Int
        get() = R.layout.activity_custom

    override fun onResume() {
        super.onResume()
        when (mType) {
            Constants.TYPE_CLASS, Constants.TYPE_GRADE -> mURL.setText(LocalHelper.getUniversity(this).cmsURL)
            Constants.TYPE_BORROW, Constants.TYPE_SEARCH -> mURL.setText(LocalHelper.getUniversity(this).libURL)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewGroup.removeAllViews()
        mWebView.destroy()
    }

    override fun setPresenter(p: CustomContract.Presenter) {
        mPresenter = p
    }

    companion object {

        private val KEY_TYPE = "type"

        fun actionStart(context: Context, type: String) {
            val intent = Intent(context, CustomActivity::class.java)
            intent.putExtra(KEY_TYPE, type)
            context.startActivity(intent)
        }
    }
}
