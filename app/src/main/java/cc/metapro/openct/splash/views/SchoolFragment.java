package cc.metapro.openct.splash.views;


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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.splash.schoolselection.SchoolSelectionActivity;

public class SchoolFragment extends Fragment implements SplashContract.SchoolView {

    @BindView(R.id.selection)
    TextView mSelection;
    @BindView(R.id.week)
    Spinner mWeek;

    private SplashContract.Presenter mPresenter;

    private boolean initialed = false;

    public static SchoolFragment getInstance() {
        return new SchoolFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school, container, false);
        ButterKnife.bind(this, view);
        initialed = true;
        return view;
    }

    @OnClick(R.id.selection)
    public void onClick() {
        startActivityForResult(
                new Intent(getContext(), SchoolSelectionActivity.class),
                SchoolSelectionActivity.REQUEST_SCHOOL_NAME);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (!isVisibleToUser && initialed) {
            int week = mWeek.getSelectedItemPosition() + 1;
            mPresenter.setSelectedWeek(week);
        }
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == AppCompatActivity.RESULT_OK) {
            if (data != null) {
                mPresenter.setSelectedSchool(data.getStringExtra(SchoolSelectionActivity.SCHOOL_RESULT));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setPresenter(SplashContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showSelectedSchool(String name) {
        mSelection.setText(name);
    }
}
