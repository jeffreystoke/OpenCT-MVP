package cc.metapro.openct.classdetail;

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
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.Spinner;

import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.classinfo.ClassInfo;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class ClassDetailActivity extends AppCompatActivity implements ClassDetailContract.View {

    private static EnrichedClassInfo mInfo;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.fab)
    FloatingActionButton mFab;

    @BindView(R.id.appbar_image)
    ImageView mImageView;

    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;

    @BindView(R.id.collapsing_toolbar_layout)
    CollapsingToolbarLayout mCollapsingToolbarLayout;

    @BindView(R.id.week)
    Spinner mDayOfWeekSpinner;

    private ClassDetailAdapter mDetailAdapter;
    private ClassDetailContract.Presenter mPresenter;
    private boolean editEnabled = false;

    public static void actionStart(Context context, EnrichedClassInfo info) {
        mInfo = info;
        Intent intent = new Intent(context, ClassDetailActivity.class);
        context.startActivity(intent);
    }

    @OnClick(R.id.appbar_image)
    public void showColorPicker() {
        ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.choose_background, getResources().getIntArray(R.array.class_background), mInfo.getColor(), 4, 8);
        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                mInfo.setColor(color);
                mImageView.setBackgroundColor(color);
                mCollapsingToolbarLayout.setContentScrimColor(color);
                classInfoModified();
            }
        });
        dialog.show(getFragmentManager(), "color_picker");
    }

    @OnClick(R.id.fab)
    public void toggleEnable() {
        if (editEnabled) {
            editEnabled = false;
            mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_edit));
//            mInfo.setDayOfWeek(mDayOfWeekSpinner.getSelectedItemPosition() + 1);
            mDayOfWeekSpinner.setEnabled(false);
            mDetailAdapter.disableEdit();
            classInfoModified();
        } else {
            editEnabled = true;
            mFab.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_dest_ok));
            mDayOfWeekSpinner.setEnabled(true);
            mDetailAdapter.enableEdit();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mImageView.setBackgroundColor(mInfo.getColor());
        mCollapsingToolbarLayout.setContentScrimColor(mInfo.getColor());
        mDetailAdapter = new ClassDetailAdapter(this, mInfo);
        if (!mDetailAdapter.isAddClass()) {
//            mDayOfWeekSpinner.setSelection(mInfo.getDayOfWeek() - 1, true);
            mDayOfWeekSpinner.setEnabled(false);
        } else {
            toggleEnable();
        }
        setRecyclerView();
        new ClassDetailPresenter(this, this);
    }

    void classInfoModified() {
        ClassInfo info = mDetailAdapter.getResultClass();
//        mInfo.setClassInfo(info);
        mPresenter.storeClassInfo(mInfo);
    }

    private void setRecyclerView() {
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mDetailAdapter);
        mRecyclerView.setItemViewSwipeEnabled(true);
        mRecyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(final int position) {
                final ClassInfo toDel = mDetailAdapter.getItem(position);
                mDetailAdapter.removeItem(position);
                mDetailAdapter.notifyDataSetChanged();
                classInfoModified();
//                Snackbar.make(mRecyclerView, toDel.getName() + " 已删除", BaseTransientBottomBar.LENGTH_INDEFINITE)
//                        .setAction(R.string.cancel, new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mDetailAdapter.addItem(position, toDel);
//                                mDetailAdapter.notifyDataSetChanged();
//                                mRecyclerView.smoothScrollToPosition(position);
//                                mRecyclerView.postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        classInfoModified();
//                                    }
//                                }, 500);
//                            }
//                        }).show();
            }
        });
    }

    @Override
    public void setPresenter(ClassDetailContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
