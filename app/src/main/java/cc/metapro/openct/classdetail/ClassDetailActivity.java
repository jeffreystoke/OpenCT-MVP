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
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;
import com.yanzhenjie.recyclerview.swipe.touch.OnItemMoveListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.item.classinfo.ClassTime;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.RecyclerViewHelper;

public class ClassDetailActivity extends AppCompatActivity {

    private static final String TAG = ClassDetailActivity.class.getName();

    static EnrichedClassInfo mInfoEditing;

    static List<ClassTime> classTimes;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_view)
    SwipeMenuRecyclerView mRecyclerView;

    @BindView(R.id.content)
    MaterialEditText mName;

    @BindView(R.id.type)
    MaterialEditText mType;

    private ClassDetailAdapter mDetailAdapter;

    public static void actionStart(Context context, String name) {
        mInfoEditing = DBManger.getInstance(context).getSingleClass(name);
        if (mInfoEditing == null) {
            mInfoEditing = new EnrichedClassInfo(
                    context.getString(R.string.new_class),
                    context.getString(R.string.mandatory),
                    new ClassTime()
            );
        }

        classTimes = new ArrayList<>(mInfoEditing.getTimeSet());
        Collections.sort(classTimes);

        Intent intent = new Intent(context, ClassDetailActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setTitle("");

        mName.setText(mInfoEditing.getName());
        mType.setText(mInfoEditing.getType());
        setRecyclerView();
    }

    private void setRecyclerView() {
        mDetailAdapter = new ClassDetailAdapter(this);
        RecyclerViewHelper.setRecyclerView(this, mRecyclerView, mDetailAdapter);
        mRecyclerView.setItemViewSwipeEnabled(true);
        mRecyclerView.setOnItemMoveListener(new OnItemMoveListener() {
            @Override
            public boolean onItemMove(int fromPosition, int toPosition) {
                return false;
            }

            @Override
            public void onItemDismiss(final int position) {
                final ClassTime toRemove = classTimes.get(position);
                classTimes.remove(toRemove);
                mDetailAdapter.notifyDataSetChanged();
                final String message = mInfoEditing.getName() + " " + DateHelper.weekDayToChinese(toRemove.getWeekDay()) + " " + toRemove.getTime() + " 节";
                final Snackbar snackbar = Snackbar.make(mRecyclerView, message + " " + getString(R.string.deleted), BaseTransientBottomBar.LENGTH_INDEFINITE);
                snackbar.setAction(android.R.string.cancel, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        classTimes.add(toRemove);
                        mDetailAdapter.notifyDataSetChanged();
                        snackbar.dismiss();
                        Snackbar.make(mRecyclerView, message + " " + getString(R.string.restored), BaseTransientBottomBar.LENGTH_LONG).show();
                        mRecyclerView.smoothScrollToPosition(classTimes.size() - 1);
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
            if (!classTimes.isEmpty()) {
                classTimes.add(0, new ClassTime(classTimes.get(classTimes.size() - 1)));
            } else {
                classTimes.add(0, new ClassTime());
            }
            mDetailAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Set<ClassTime> timeSet = new ArraySet<>();
        mInfoEditing.setType(mType.getText().toString());
        timeSet.addAll(classTimes);
        mInfoEditing.setTimes(timeSet);
        try {
            DBManger.getInstance(this).updateSingleClass(mInfoEditing.getName(), mName.getText().toString(), mInfoEditing);
        } catch (Exception e) {
            Log.d(TAG, e.getMessage(), e);
            return;
        }
        super.onBackPressed();
    }

}
