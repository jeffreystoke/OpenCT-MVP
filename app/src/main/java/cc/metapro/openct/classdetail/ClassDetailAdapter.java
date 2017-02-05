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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.utils.REHelper;

class ClassDetailAdapter extends RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder> {

    private static boolean addClass = false;
    private LayoutInflater mInflater;
    private List<ClassInfo> mClasses;
    private SparseArray<ClassDetailViewHolder> mViewHolders;

    ClassDetailAdapter(Context context, EnrichedClassInfo info) {
        mInflater = LayoutInflater.from(context);
        mClasses = info.getAllClasses();
        addClass = false;
        if (mClasses.isEmpty()) {
            mClasses.add(new ClassInfo());
            addClass = true;
        }
        mViewHolders = new SparseArray<>(mClasses.size());
    }

    boolean isAddClass() {
        return addClass;
    }

    void enableEdit() {
        for (int i = 0; i < mViewHolders.size(); i++) {
            mViewHolders.get(i).enableEdit();
        }
    }

    void disableEdit() {
        for (int i = 0; i < mViewHolders.size(); i++) {
            mViewHolders.get(i).disableEdit();
        }
    }

    ClassInfo getItem(int i) {
        return mClasses.get(i);
    }

    void addItem(int i, ClassInfo info) {
        mClasses.add(i, info);
    }

    void removeItem(int i) {
        mClasses.remove(i);
        mViewHolders.remove(i);
    }

    @Nullable
    ClassInfo getResultClass() {
        ClassInfo result = null;
        ClassInfo tmp = null;
        for (int i = 0; i < mViewHolders.size(); i++) {
            if (i == 0) {
                result = mViewHolders.valueAt(i).getClassInfo();
                tmp = result;
            } else {
                ClassDetailViewHolder viewHolder = mViewHolders.valueAt(i);
                if (viewHolder != null) {
                    tmp.setSubClassInfo(viewHolder.getClassInfo());
                    tmp = tmp.getSubClassInfo();
                }
            }
        }
        return result;
    }

    @Override
    public ClassDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_class_detail, parent, false);
        return new ClassDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassDetailViewHolder holder, int position) {
        holder.setInfo(mClasses.get(position));
        mViewHolders.put(position, holder);
    }

    @Override
    public int getItemCount() {
        return mClasses.size();
    }

    class ClassDetailViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.class_name)
        MaterialEditText mName;
        @BindView(R.id.class_type)
        MaterialEditText mType;
        @BindView(R.id.odd)
        RadioButton mOddRadio;
        @BindView(R.id.even)
        RadioButton mEvenRadio;
        @BindView(R.id.common)
        RadioButton mCommonRadio;
        @BindView(R.id.class_teacher)
        MaterialEditText mTeacher;
        @BindView(R.id.class_place)
        MaterialEditText mClassPlace;
        @BindView(R.id.time_start)
        MaterialEditText mTimeStart;
        @BindView(R.id.time_end)
        MaterialEditText mTimeEnd;
        @BindView(R.id.week_start)
        MaterialEditText mWeekStart;
        @BindView(R.id.week_end)
        MaterialEditText mWeekEnd;

        private String id;

        ClassDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setInfo(@NonNull ClassInfo info) {
            id = info.getId();
            if (!addClass) {
                mName.setText(info.getName());
                mType.setText(info.getType());
                mTeacher.setText(info.getTeacher());

                int[] timeStartEnd = REHelper.getStartEnd(info.getTime());
                mTimeStart.setText(timeStartEnd[0] + "");
                mTimeEnd.setText(timeStartEnd[1] + "");

                int[] duringStartEnd = REHelper.getStartEnd(info.getDuring());
                mWeekStart.setText(duringStartEnd[0] + "");
                mWeekEnd.setText(duringStartEnd[1] + "");

                mClassPlace.setText(info.getPlace());

                if (info.isEvenWeek()) {
                    mEvenRadio.setChecked(true);
                } else if (info.isOddWeek()) {
                    mOddRadio.setChecked(true);
                } else {
                    mCommonRadio.setChecked(true);
                }

                disableEdit();
            }
        }

        void enableEdit() {
            mName.setEnabled(true);
            mType.setEnabled(true);
            mOddRadio.setEnabled(true);
            mEvenRadio.setEnabled(true);
            mCommonRadio.setEnabled(true);
            mTeacher.setEnabled(true);
            mClassPlace.setEnabled(true);
            mTimeStart.setEnabled(true);
            mTimeEnd.setEnabled(true);
            mWeekStart.setEnabled(true);
            mWeekEnd.setEnabled(true);
        }

        void disableEdit() {
            mName.setEnabled(false);
            mType.setEnabled(false);
            mOddRadio.setEnabled(false);
            mEvenRadio.setEnabled(false);
            mCommonRadio.setEnabled(false);
            mTeacher.setEnabled(false);
            mClassPlace.setEnabled(false);
            mTimeStart.setEnabled(false);
            mTimeEnd.setEnabled(false);
            mWeekStart.setEnabled(false);
            mWeekEnd.setEnabled(false);
        }

        ClassInfo getClassInfo() {
            String name = mName.getText().toString();
            String during = mWeekStart.getText().toString() + " - " + mWeekEnd.getText().toString();
            String time = mTimeStart.getText().toString() + " - " + mTimeEnd.getText().toString();
            if (!REHelper.isEmpty(name) && !REHelper.isEmpty(during) && !REHelper.isEmpty(time)) {
                String type = mType.getText().toString();
                String teacher = mTeacher.getText().toString();
                String place = mClassPlace.getText().toString();
                return new ClassInfo(id, name, type, time, during, teacher, place, mOddRadio.isChecked(), mEvenRadio.isChecked());
            } else {
                Toast.makeText(mName.getContext(), "请输入课程名称, 上课时间, 课程周期\n(这些都很重要)", Toast.LENGTH_LONG).show();
            }
            return null;
        }
    }
}
