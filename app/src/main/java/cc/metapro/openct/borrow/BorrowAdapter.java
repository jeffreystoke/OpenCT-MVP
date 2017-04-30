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
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import cc.metapro.openct.data.university.model.BorrowInfo;

@Keep
class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder> {

    private final LayoutInflater mInflater;
    private List<BorrowInfo> mBorrows;

    BorrowAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mBorrows = new ArrayList<>(0);
    }

    @Override
    public BorrowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_borrow, parent, false);
        return new BorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BorrowViewHolder holder, int position) {
        holder.setInfo(mBorrows.get(position));
    }

    @Override
    public int getItemCount() {
        return mBorrows.size();
    }

    void setNewBorrows(@NonNull List<BorrowInfo> borrows) {
        mBorrows = borrows;
    }

    static class BorrowViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.borrow_item_title)
        TextView mTitle;

        BorrowViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setInfo(@Nullable BorrowInfo info) {
            if (info != null) {
                mTitle.setText(info.getFilteredContent(BorrowPresenter.list));
            }
        }
    }

}
