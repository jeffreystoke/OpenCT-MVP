package cc.metapro.openct.utils

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
import android.support.annotation.AttrRes
import android.support.annotation.ColorInt
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue

/**
 * 完成一个纵向布局的 RecyclerView 设置
 * @param context
 * @param recyclerView 将被设置的 RecyclerView
 * @param adapter RecyclerView 适配器
 * @return 纵向线性布局管理器
 */
fun setRecyclerView(context: Context, recyclerView: RecyclerView, adapter: RecyclerView.Adapter<*>): LinearLayoutManager {
    val manager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    recyclerView.layoutManager = manager
    recyclerView.itemAnimator = DefaultItemAnimator()
    recyclerView.adapter = adapter
    return manager
}

/**
 * 获取主题内包含的颜色
 *
 */
@ColorInt
fun getThemeColor(context: Context, @AttrRes attr: Int): Int {
    val typedValue = TypedValue()
    val theme = context.theme
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}
