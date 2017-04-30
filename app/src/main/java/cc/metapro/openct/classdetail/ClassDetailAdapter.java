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
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.classinfo.ClassTime;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.ReferenceUtils;

class ClassDetailAdapter extends RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder> {

    private LayoutInflater mInflater;
    private List<ClassTime> mClassTimes;

    ClassDetailAdapter(Context context, List<ClassTime> classTimes) {
        mInflater = LayoutInflater.from(context);
        mClassTimes = classTimes;
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
        return mClassTimes == null ? 0 : mClassTimes.size();
    }

    class ClassDetailViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.week_day)
        Spinner mWeekDay;
        @BindView(R.id.time_start)
        MaterialEditText mTimeStart;
        @BindView(R.id.time_end)
        MaterialEditText mTimeEnd;
        @BindView(R.id.during_container)
        LinearLayout mDuringContainer;
        TextView[] selections = new TextView[Constants.WEEKS];
        @BindView(R.id.class_teacher)
        MaterialEditText mTeacher;
        @BindView(R.id.class_place)
        MaterialEditText mPlace;
        @BindView(R.id.edit)
        TextView mEdit;

        private int position = 0;

        ClassDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void setEditable(boolean isEditable) {
            mWeekDay.setEnabled(isEditable);
            mTimeStart.setEnabled(isEditable);
            mTimeEnd.setEnabled(isEditable);
            mTeacher.setEnabled(isEditable);
            mPlace.setEnabled(isEditable);
            if (selections != null) {
                for (TextView textView : selections) {
                    textView.setEnabled(isEditable);
                }
            }
            if (isEditable) {
                mEdit.setText(R.string.save);
                mEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setEditable(false);
                        ClassTime time = mClassTimes.get(position);
                        mClassTimes.remove(time);
                        time = getInfo(time);
                        mClassTimes.add(position, time);
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

        // set info and react to during click
        void setInfo(final int position) {
            this.position = position;
            final ClassTime time = mClassTimes.get(position);
            mTimeStart.setText("" + time.getDailySeq());
            mTimeEnd.setText("" + time.getDailyEnd());
            mWeekDay.setSelection(time.getWeekDay() - 1, true);
            mTeacher.setText(time.getTeacher());
            mPlace.setText(time.getPlace());

            final Context context = mDuringContainer.getContext();

            mDuringContainer.removeAllViews();
            for (int i = 0; i < Constants.WEEKS / 6; i++) {
                LinearLayout linearLayout = new LinearLayout(mDuringContainer.getContext());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);

                mDuringContainer.addView(linearLayout);
                ViewGroup.LayoutParams params = linearLayout.getLayoutParams();
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                linearLayout.setLayoutParams(params);

                for (int j = 0; j < 6; j++) {
                    final int week = i * 6 + j + 1;
                    final TextView textView = new TextView(context);
                    textView.setText(week + "");
                    textView.setGravity(Gravity.CENTER);
                    if (time.hasClass(week)) {
                        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue));
                        textView.setTextColor(ReferenceUtils.getThemeColor(context, R.attr.colorAccent));
                    } else {
                        textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey));
                        textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey));
                    }

                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mClassTimes.remove(time);
                            if (time.hasClass(week)) {
                                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_grey));
                                textView.setTextColor(ContextCompat.getColor(context, R.color.material_grey));
                                time.disableWeek(week);
                            } else {
                                textView.setBackground(ContextCompat.getDrawable(context, R.drawable.text_view_card_style_blue));
                                textView.setTextColor(ReferenceUtils.getThemeColor(context, R.attr.colorAccent));
                                time.enableWeek(week);
                            }
                            mClassTimes.add(position, time);
                        }
                    });
                    textView.setLines(1);
                    linearLayout.addView(textView, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    selections[week - 1] = textView;
                }
            }
            setEditable(false);
        }

        ClassTime getInfo(ClassTime oldTime) {
            int weekDay = mWeekDay.getSelectedItemPosition() + 1;
            int dailySeq = Integer.parseInt(mTimeStart.getText().toString());
            int dailyEnd = Integer.parseInt(mTimeEnd.getText().toString());

            oldTime.setPlace(mPlace.getText().toString());
            oldTime.setTeacher(mTeacher.getText().toString());
            oldTime.setDailySeq(dailySeq);
            oldTime.setWeekDay(weekDay);
            oldTime.setDailyEnd(dailyEnd);
            return oldTime;
        }
    }
}
