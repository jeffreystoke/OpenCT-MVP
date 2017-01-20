package cc.metapro.openct.room;

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

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.utils.ActivityUtils;

public class RoomActivity extends AppCompatActivity {

    @BindView(R.id.room_toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab_filter)
    FloatingActionButton mFab;
    private RoomContract.Presenter mPresenter;
    private RoomFragment mRoomFragment;

    @OnClick(R.id.fab_filter)
    public void filter() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_room_filter, null);

        final Spinner places = (Spinner) view.findViewById(R.id.room_filter_place);
        final Spinner week = (Spinner) view.findViewById(R.id.room_filter_week);
        final Spinner day = (Spinner) view.findViewById(R.id.room_filter_day);
        final Spinner time = (Spinner) view.findViewById(R.id.room_filter_time);

        ab.setView(view)
                .setTitle("筛选空教室")
                .setPositiveButton("筛选", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String p = places.getSelectedItem().toString();
                        int w = week.getSelectedItemPosition() + 1;
                        int d = day.getSelectedItemPosition() + 1;
                        int t = time.getSelectedItemPosition() + 1;
                        String we = week.getSelectedItem().toString();
                        String da = day.getSelectedItem().toString();
                        String ti = time.getSelectedItem().toString();
                        mRoomFragment.showRooms(w, d, t, p);
                        setTitle("筛选条件: " + we + " " + da + ", " + ti);
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        mRoomFragment =
                (RoomFragment) fm.findFragmentById(R.id.room_filter_container);

        if (mRoomFragment == null) {
            mRoomFragment = new RoomFragment();
            ActivityUtils.addFragmentToActivity(fm, mRoomFragment, R.id.room_filter_container);
        }

        mPresenter = new RoomPresenter(mRoomFragment);
    }

    @Override
    protected void onDestroy() {
        mPresenter.storeRoomInfos(this);
        super.onDestroy();
    }
}
