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
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.BookInfo
import java.util.*

internal class BooksAdapter(private val mContext: Context) : RecyclerView.Adapter<BooksAdapter.BookInfoViewHolder>() {

    private var mBooks: MutableList<BookInfo>? = null

    init {
        mBooks = ArrayList<BookInfo>(0)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookInfoViewHolder {
        val layoutInflater = LayoutInflater.from(mContext)
        val view = layoutInflater.inflate(R.layout.item_book, parent, false)
        return BookInfoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookInfoViewHolder, position: Int) {
        val b = mBooks!![position]
        holder.setTitle(b.mTitle)
        holder.setAuthor(b.mAuthor)
        holder.setContent(b.mContent)
        holder.setStoreInfo(b.mStoreInfo)
        holder.setLoadRaw(mContext, b.mLink)
    }

    fun addBooks(books: List<BookInfo>) {
        for (b in books) {
            mBooks!!.add(b)
        }
    }

    fun setBooks(books: MutableList<BookInfo>?) {
        if (books != null) {
            mBooks = books
        } else {
            mBooks = ArrayList<BookInfo>(0)
        }
    }

    override fun getItemCount(): Int {
        return mBooks!!.size
    }

    internal class BookInfoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @BindView(R.id.book_title)
        var mTitle: TextView? = null

        @BindView(R.id.author)
        var mAuthor: TextView? = null

        @BindView(R.id.content)
        var mContent: TextView? = null

        @BindView(R.id.store_info)
        var mStoreInfo: TextView? = null

        @BindView(R.id.load_raw)
        var mLink: TextView? = null

        init {
            ButterKnife.bind(this, itemView)
        }

        fun setTitle(title: String) {
            mTitle!!.text = title
        }

        fun setAuthor(author: String) {
            mAuthor!!.text = author
        }

        fun setContent(content: String) {
            mContent!!.text = content
        }

        fun setStoreInfo(storeInfo: String) {
            mStoreInfo!!.text = storeInfo
        }

        fun setLoadRaw(context: Context, link: String) {
            mLink!!.setOnClickListener { BookDetailActivity.actionStart(context, mTitle!!.text.toString(), link) }
        }
    }
}
