package cc.metapro.openct.utils.base

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

import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.LayoutRes
import android.support.annotation.StyleRes
import android.support.v7.app.AppCompatActivity
import cc.metapro.openct.R
import cc.metapro.openct.utils.PrefHelper


abstract class BaseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val theme = PrefHelper.getInt(this, R.string.pref_theme_activity, R.style.AppTheme_Colorful_Light)
        setTheme(theme)
        super.onCreate(savedInstanceState)
        setContentView(layout)
    }

    @get:LayoutRes
    protected abstract val layout: Int

    protected abstract val presenter: BasePresenter?

    override fun onResume() {
        super.onResume()
        presenter?.subscribe()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter?.unSubscribe()
    }
}
