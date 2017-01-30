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

import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.RecyclerViewHelper;

@Keep
public class LibBorrowFragment extends Fragment implements LibBorrowContract.View {

    private static final String TAG = "openct_borrow_view";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private BorrowAdapter mBorrowAdapter;
    private LibBorrowContract.Presenter mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib_borrow, container, false);
        ButterKnife.bind(this, view);
        mBorrowAdapter = new BorrowAdapter(getContext());
        RecyclerViewHelper.setRecyclerView(getContext(), mRecyclerView, mBorrowAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void setPresenter(LibBorrowContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showDue(@NonNull List<BorrowInfo> borrows) {
        try {
            List<BorrowInfo> dueInfo = new ArrayList<>(borrows.size());
            for (BorrowInfo b : borrows) {
                if (b.isExceeded()) {
                    dueInfo.add(b);
                }
            }
            mBorrowAdapter.setNewBorrows(dueInfo);
            mBorrowAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void showAll(List<BorrowInfo> borrows) {
        try {
            mBorrowAdapter.setNewBorrows(borrows);
            mBorrowAdapter.notifyDataSetChanged();
            ActivityUtils.dismissProgressDialog();
            Snackbar.make(mRecyclerView, "共有 " + borrows.size() + " 条借阅信息", BaseTransientBottomBar.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
