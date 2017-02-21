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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.classinfo.ClassTime;

import static cc.metapro.openct.classdetail.ClassDetailActivity.classTimes;

class ClassDetailAdapter extends RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder> {

    private LayoutInflater mInflater;

    ClassDetailAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ClassDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_class_detail, parent, false);
        return new ClassDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassDetailViewHolder holder, int position) {
        holder.setInfo(position);
    }

    @Override
    public int getItemCount() {
        return classTimes == null ? 0 : classTimes.size();
    }

    static class ClassDetailViewHolder extends RecyclerView.ViewHolder {

        // 星期
        @BindView(R.id.week_day)
        Spinner mWeekDay;

        // 时间
        @BindView(R.id.time_start)
        MaterialEditText mTimeStart;
        @BindView(R.id.time_end)
        MaterialEditText mTimeEnd;

        // 周期
        @BindView(R.id.during_grid)
        GridLayout mDuringGrid;
        TextView[][] selections = new TextView[5][6];

        // 教师
        @BindView(R.id.class_teacher)
        MaterialEditText mTeacher;

        // 地点
        @BindView(R.id.class_place)
        MaterialEditText mPlace;

        @BindView(R.id.edit)
        TextView mEdit;

        private int position = 0;

        private void setEditable(boolean isEditable) {
            mWeekDay.setEnabled(isEditable);
            mTimeStart.setEnabled(isEditable);
            mTimeEnd.setEnabled(isEditable);
            mTeacher.setEnabled(isEditable);
            mPlace.setEnabled(isEditable);
            if (isEditable) {
                mEdit.setText(R.string.save);
                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEditable(false);
                        ClassTime time = classTimes.get(position);
                        classTimes.remove(time);
                        time = getInfo(time);
                        classTimes.add(position, time);
                    }
                });
            } else {
                mEdit.setText(R.string.edit);
                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEditable(true);
                    }
                });
            }
        }

        ClassDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        // set info and react to during click
        void setInfo(final int position) {
            this.position = position;
            final ClassTime time = classTimes.get(position);
            mTimeStart.setText("" + time.getDailySeq());
            mTimeEnd.setText("" + (time.getDailySeq() + time.getLength() - 1));
            mWeekDay.setSelection(time.getWeekDay() - 1, true);
            mTeacher.setText(time.getTeacher());
            mPlace.setText(time.getPlace());

            final Context context = mDuringGrid.getContext();
            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 6; j++) {
                    final int week = i * 6 + j + 1;
                    final TextView textView = new TextView(context);
                    textView.setText("第" + week + "周");
                    textView.setGravity(Gravity.CENTER);
                    if (time.hasClass(week)) {
                        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.azure));
                    } else {
                        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey));
                    }

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            classTimes.remove(time);
                            if (time.hasClass(week)) {
                                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey));
                                time.disableWeek(week);
                            } else {
                                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.azure));
                                time.enableWeek(week);
                            }
                            classTimes.add(position, time);
                        }
                    });
                    GridLayout.Spec row = GridLayout.spec(i, 1, 1);
                    GridLayout.Spec col = GridLayout.spec(j, 1, 1);
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams(row, col);
                    mDuringGrid.addView(textView, params);
                    selections[i][j] = textView;
                }
            }
            setEditable(false);
        }

        ClassTime getInfo(ClassTime oldTime) {
            int weekDay = mWeekDay.getSelectedItemPosition() + 1;
            int dailySeq = Integer.parseInt(mTimeStart.getText().toString());
            int length = Integer.parseInt(mTimeEnd.getText().toString()) - dailySeq + 1;

            oldTime.setPlace(mPlace.getText().toString());
            oldTime.setTeacher(mTeacher.getText().toString());
            oldTime.setDailySeq(dailySeq);
            oldTime.setWeekDay(weekDay);
            oldTime.setLength(length);
            return oldTime;
        }
    }
}
