package cc.metapro.openct.borrow;

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
import cc.metapro.openct.data.university.item.BorrowInfo;

public class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder> {

    private List<BorrowInfo> mBorrows;

    private Context mContext;

    public BorrowAdapter(Context context) {
        mContext = context;
        mBorrows = new ArrayList<>(0);
    }

    @Override
    public BorrowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_borrow, parent, false);
        return new BorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BorrowViewHolder holder, int position) {
        BorrowInfo b = mBorrows.get(position);
        holder.setTitle(b.getBookTitle());
        holder.setAuthor(b.getAuthor());
        holder.setContent(b.getContent());
        holder.setBorrowTime(b.getBorrowDate());
        holder.setDueTime(b.getDueDate());
    }

    @Override
    public int getItemCount() {
        return mBorrows.size();
    }

    public void setNewBorrows(List<BorrowInfo> borrows) {
        if (borrows == null || borrows.size() == 0) {
            mBorrows = new ArrayList<>(0);
        } else {
            mBorrows = borrows;
        }
    }

    public static class BorrowViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.lib_borrow_item_title)
        TextView mTitle;

        @BindView(R.id.lib_borrow_item_author)
        TextView mAuthor;

        @BindView(R.id.lib_borrow_item_borrow_content)
        TextView mContent;

        @BindView(R.id.lib_borrow_item_borrow_time)
        TextView mBorrowTime;

        @BindView(R.id.lib_borrow_item_due_time)
        TextView mDueTime;

        public BorrowViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        public void setAuthor(String author) {
            mAuthor.setText(author);
        }

        public void setContent(String content) {
            mContent.setText(content);
        }

        public void setBorrowTime(String borrowTime) {
            mBorrowTime.setText(borrowTime);
        }

        public void setDueTime(String dueTime) {
            mDueTime.setText(dueTime);
        }
    }
}
