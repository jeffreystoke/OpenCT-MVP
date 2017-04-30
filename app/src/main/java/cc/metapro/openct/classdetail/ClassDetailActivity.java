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
import android.support.annotation.ColorInt;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArraySet;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.university.model.classinfo.ClassTime;
import cc.metapro.openct.data.university.model.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.RecyclerViewHelper;
import cc.metapro.openct.utils.base.BaseActivity;

public class ClassDetailActivity extends BaseActivity {

    private static final String KEY_CLASS_NAME = "class_name";
    private static final String TAG = ClassDetailActivity.class.getName();

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;
    @BindView(R.id.content)
    MaterialEditText mName;
    @BindView(R.id.type)
    MaterialEditText mType;
    @BindView(R.id.bg)
    FloatingActionButton mBackground;

    private ClassDetailAdapter mDetailAdapter;
    private EnrichedClassInfo mInfoEditing;
    private List<ClassTime> mClassTimes;
    private String mOldName;

    public static void actionStart(Context context, String name) {
        Intent intent = new Intent(context, ClassDetailActivity.class);
        intent.putExtra(KEY_CLASS_NAME, name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_class_detail;
    }

    @OnClick(R.id.bg)
    void showColorPicker() {
        ColorPickerDialog dialog = ColorPickerDialog.newBuilder().setColor(mInfoEditing.getColor()).create();
        dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int dialogId, @ColorInt int color) {
                mInfoEditing.setColor(color);
                mBackground.setColorFilter(color);
            }

            @Override
            public void onDialogDismissed(int dialogId) {

            }
        });
        dialog.show(getFragmentManager(), "color_picker");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOldName = getIntent().getStringExtra(KEY_CLASS_NAME);
        mInfoEditing = DBManger.getInstance(this).getSingleClass(mOldName);
        if (mInfoEditing == null) {
            mInfoEditing = new EnrichedClassInfo(getString(R.string.new_class), getString(R.string.mandatory), new ClassTime());
        }

        mClassTimes = new ArrayList<>(mInfoEditing.getTimeSet());
        Collections.sort(mClassTimes);

        mName.setText(mInfoEditing.getName());
        mType.setText(mInfoEditing.getType());
        mBackground.setColorFilter(mInfoEditing.getColor());
        setRecyclerView();
    }

    private void setRecyclerView() {
        mDetailAdapter = new ClassDetailAdapter(this, mClassTimes);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mDetailAdapter);
        mRecyclerView.setItemViewSwipeEnabled(true);
        mRecyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(final int position) {
                final ClassTime toRemove = mClassTimes.get(position);
                mClassTimes.remove(toRemove);
                mDetailAdapter.notifyDataSetChanged();
                final String prefix = mName.getText().toString() + " " +
                        DateHelper.weekDayTrans(ClassDetailActivity.this, toRemove.getWeekDay()) + " " +
                        toRemove.getTimeString() + " ";

                String msg = prefix + getString(R.string.deleted);
                final Snackbar snackbar = Snackbar.make(mRecyclerView, msg, BaseTransientBottomBar.LENGTH_INDEFINITE);
                snackbar.setAction(android.R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mClassTimes.add(toRemove);
                        mDetailAdapter.notifyDataSetChanged();
                        snackbar.dismiss();

                        String msg = prefix + getString(R.string.restored);

                        Snackbar.make(mRecyclerView, msg, BaseTransientBottomBar.LENGTH_LONG).show();
                        mRecyclerView.smoothScrollToPosition(mClassTimes.size() - 1);
                    }
                });
                snackbar.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.class_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.add) {
            if (!mClassTimes.isEmpty()) {
                mClassTimes.add(0, new ClassTime(mClassTimes.get(mClassTimes.size() - 1)));
            } else {
                mClassTimes.add(0, new ClassTime());
            }
            mDetailAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mInfoEditing.setName(mName.getText().toString());
        mInfoEditing.setType(mType.getText().toString());
        mInfoEditing.setTimes(new ArraySet<>(mClassTimes));
        try {
            if (mClassTimes == null || mClassTimes.isEmpty()) {
                try {
                    DBManger.getInstance(this).updateSingleClass(mOldName, "", null);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage(), e);
                }
            } else {
                try {
                    DBManger.getInstance(this).updateSingleClass(mOldName, mInfoEditing.getName(), mInfoEditing);
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage(), e);
                    Toast.makeText(this, R.string.class_with_same_name, Toast.LENGTH_LONG).show();
                    return;
                }
            }
        } finally {
            Constants.sClasses.setInfoByName(mOldName, mInfoEditing);
        }
        super.onBackPressed();
    }
}
