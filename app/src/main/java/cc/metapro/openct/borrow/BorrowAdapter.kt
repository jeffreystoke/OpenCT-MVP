package cc.metapro.openct.borrow

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
import cc.metapro.openct.R
import cc.metapro.openct.data.university.model.BorrowInfo
import java.util.*

internal class BorrowAdapter(context: Context) : RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder>() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var mBorrows: List<BorrowInfo> = ArrayList(0)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BorrowViewHolder {
        val view = mInflater.inflate(R.layout.item_borrow, parent, false)
        return BorrowViewHolder(view)
    }

    override fun onBindViewHolder(holder: BorrowViewHolder, position: Int) {
        holder.setInfo(mBorrows[position])
    }

    override fun getItemCount(): Int {
        return mBorrows.size
    }

    fun setNewBorrows(borrows: List<BorrowInfo>) {
        mBorrows = borrows
    }

    internal class BorrowViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var mTitle: TextView

        fun setInfo(info: BorrowInfo?) {
            if (info != null) {
                mTitle.text = info.getFilteredContent(BorrowPresenter.list)
            }
        }
    }

}
