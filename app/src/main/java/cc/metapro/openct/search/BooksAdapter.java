package cc.metapro.openct.search;

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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.BookInfo;

class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookInfoViewHolder> {

    private Context mContext;

    private List<BookInfo> mBooks;

    BooksAdapter(Context context) {
        mContext = context;
        mBooks = new ArrayList<>(0);
    }

    @Override
    public BookInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_book, parent, false);
        return new BookInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookInfoViewHolder holder, int position) {
        BookInfo b = mBooks.get(position);
        holder.setTitle(b.mTitle);
        holder.setAuthor(b.mAuthor);
        holder.setContent(b.mContent);
        holder.setStoreInfo(b.mStoreInfo);
        holder.setLoadRaw(mContext, b.mLink);
    }

    void addBooks(List<BookInfo> books) {
        for (BookInfo b : books) {
            mBooks.add(b);
        }
    }

    void setBooks(List<BookInfo> books) {
        if (books != null) {
            mBooks = books;
        } else {
            mBooks = new ArrayList<>(0);
        }
    }

    @Override
    public int getItemCount() {
        return mBooks.size();
    }

    static class BookInfoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.book_title)
        TextView mTitle;

        @BindView(R.id.author)
        TextView mAuthor;

        @BindView(R.id.content)
        TextView mContent;

        @BindView(R.id.store_info)
        TextView mStoreInfo;

        @BindView(R.id.load_raw)
        TextView mLink;

        BookInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        void setAuthor(String author) {
            mAuthor.setText(author);
        }

        public void setContent(String content) {
            mContent.setText(content);
        }

        void setStoreInfo(String storeInfo) {
            mStoreInfo.setText(storeInfo);
        }

        void setLoadRaw(final Context context, final String link) {
            mLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BookDetailActivity.actionStart(context, mTitle.getText().toString(), link);
                }
            });
        }
    }
}
