package cc.metapro.openct.search

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
import android.graphics.Bitmap
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.webkit.*
import android.widget.ProgressBar
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import cc.metapro.openct.R
import cc.metapro.openct.utils.base.BaseActivity

class BookDetailActivity : BaseActivity() {
    @BindView(R.id.toolbar)
    internal var mToolbar: Toolbar? = null
    @BindView(R.id.fab_back)
    internal var mFab: FloatingActionButton? = null
    @BindView(R.id.book_detail_web)
    internal var mWebView: WebView? = null
    @BindView(R.id.book_detail_progress)
    internal var pb: ProgressBar? = null
    @BindView(R.id.activity_book_detail)
    internal var mActivityBookDetail: CoordinatorLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        val intent = intent
        val title = intent.getStringExtra(KEY_TITLE)
        val url = intent.getStringExtra(KEY_URL)

        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)

        setTitle(title)
        setWebView(url)
    }

    protected override val layout: Int
        get() = R.layout.activity_book_detail

    @OnClick(R.id.fab_back)
    fun goBack() {
        if (mWebView!!.canGoBack()) {
            mWebView!!.goBack()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setWebView(URL: String) {
        mWebView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return true
            }

            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap) {
                super.onPageStarted(view, url, favicon)
                pb!!.progress = 0
                pb!!.visibility = View.VISIBLE
            }
        }

        mWebView!!.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                if (newProgress == 100) {
                    pb!!.visibility = View.GONE
                } else {
                    if (pb!!.visibility == View.INVISIBLE) {
                        pb!!.visibility = View.VISIBLE
                    }
                    pb!!.progress = newProgress
                }
            }
        }

        mWebView!!.loadUrl(URL)
        mWebView!!.settings.javaScriptEnabled = true
        mWebView!!.settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
        mWebView!!.settings.setSupportZoom(true)
        mWebView!!.settings.useWideViewPort = true
    }

    override fun onDestroy() {
        super.onDestroy()
        mWebView = null
        mActivityBookDetail = null
    }

    companion object {

        val KEY_TITLE = "title"
        val KEY_URL = "url"

        fun actionStart(context: Context, title: String, url: String) {
            val intent = Intent(context, BookDetailActivity::class.java)
            intent.putExtra(KEY_TITLE, title)
            intent.putExtra(KEY_URL, url)
            context.startActivity(intent)
        }
    }
}
