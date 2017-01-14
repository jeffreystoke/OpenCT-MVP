package cc.metapro.openct.borrow;


/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.RecyclerViewHelper;


public class LibBorrowFragment extends Fragment implements LibBorrowContract.View {

    @BindView(R.id.lib_borrow_recycler_view)
    RecyclerView mRecyclerView;

    private Context mContext;
    private BorrowAdapter mBorrowAdapter;
    private LibBorrowContract.Presenter mPresenter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lib_borrow, container, false);
        mContext = getContext();
        ButterKnife.bind(this, view);

        mBorrowAdapter = new BorrowAdapter(mContext);
        RecyclerViewHelper.setRecyclerView(mContext, mRecyclerView, mBorrowAdapter);

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        mPresenter.start();
        super.onResume();
    }

    @Override
    public void setPresenter(LibBorrowContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showDue(List<BorrowInfo> infos) {
        if (infos != null) {
            List<BorrowInfo> dueInfo = new ArrayList<>(infos.size());
            for (BorrowInfo b : infos) {
                if (b.isExceeded()) {
                    dueInfo.add(b);
                }
            }
            mBorrowAdapter.setNewBorrows(dueInfo);
            mBorrowAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoadBorrows(List<BorrowInfo> infos) {
        mBorrowAdapter.setNewBorrows(infos);
        mBorrowAdapter.notifyDataSetChanged();
        ActivityUtils.dismissProgressDialog();
        Toast.makeText(mContext, "共有 " + infos.size() + " 条借阅信息", Toast.LENGTH_SHORT).show();
    }

}
