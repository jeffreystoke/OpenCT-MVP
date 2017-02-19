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

import android.support.annotation.Nullable;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.classinfo.ClassInfo;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.REHelper;

class ClassDetailAdapter extends RecyclerView.Adapter<ClassDetailAdapter.ClassDetailViewHolder> {

    private boolean addClass = false;
    private ClassDetailActivity mContext;
    private LayoutInflater mInflater;
    private List<ClassInfo> mClasses;
    private SparseArray<ClassDetailViewHolder> mViewHolders;

    ClassDetailAdapter(ClassDetailActivity context, EnrichedClassInfo info) {
        mInflater = LayoutInflater.from(context);
        mInflater.getContext();
//        mClasses = info.getAllClasses();
        mContext = context;
        addClass = false;
        if (mClasses.isEmpty()) {
//            mClasses.add(new ClassInfo());
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
//        ClassInfo result = null;
//        ClassInfo tmp = null;
//        for (int i = 0; i < mViewHolders.size(); i++) {
//            if (i == 0) {
//                result = mViewHolders.valueAt(i).getClassInfo();
//                tmp = result;
//            } else {
//                ClassDetailViewHolder viewHolder = mViewHolders.valueAt(i);
//                if (viewHolder != null) {
//                    if (tmp != null) {
//                        tmp.setSubClassInfo(viewHolder.getClassInfo());
//                        tmp = tmp.getSubClassInfo();
//                    }
//                }
//            }
//        }
//        return result;
        return null;
    }

    @Override
    public ClassDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_class_detail, parent, false);
        return new ClassDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassDetailViewHolder holder, int position) {
//        holder.setInfo(mClasses.get(position));
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
        @BindView(R.id.during_content)
        LinearLayout mDuringLayout;
        @BindView(R.id.add_during)
        TextView mAddDuring;
        private Map<MaterialEditText, MaterialEditText> mDuringMap = new HashMap<>();
        private String id;

        ClassDetailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.add_during)
        void addDuringEditor() {
            addDuringEditor(-1, -1);
        }

//        void setInfo(@NonNull ClassInfo info) {
//            if (!addClass) {
//                mName.setText(info.getName());
//                mType.setText(info.getType());
//                mTeacher.setText(info.getTeacher());
//
//                int[] timeStartEnd = REHelper.getStartEnd(info.getTime());
//                mTimeStart.setText(timeStartEnd[0] + "");
//                mTimeEnd.setText(timeStartEnd[1] + "");
//                List<int[]> duringStartEnd = REHelper.getAllStartEnd(info.getDuring());
//                for (int[] startEnd : duringStartEnd) {
//                    addDuringEditor(startEnd[0], startEnd[1]);
//                }
//                mClassPlace.setText(info.getPlace());
//
//                if (info.isEvenWeek()) {
//                    mEvenRadio.setChecked(true);
//                } else if (info.isOddWeek()) {
//                    mOddRadio.setChecked(true);
//                } else {
//                    mCommonRadio.setChecked(true);
//                }
//
//                disableEdit();
//            } else {
//                addDuringEditor(0, 0);
//            }
//        }

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
            mAddDuring.setEnabled(true);
            for (MaterialEditText start : mDuringMap.keySet()) {
                MaterialEditText end = mDuringMap.get(start);
                start.setEnabled(true);
                end.setEnabled(true);
            }
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
            mAddDuring.setEnabled(false);
            for (MaterialEditText start : mDuringMap.keySet()) {
                MaterialEditText end = mDuringMap.get(start);
                start.setEnabled(false);
                end.setEnabled(false);
            }
        }

        ClassInfo getClassInfo() {
            String name = mName.getText().toString();

            String during = "";
            for (MaterialEditText start : mDuringMap.keySet()) {
                MaterialEditText end = mDuringMap.get(start);
                if (!TextUtils.isEmpty(during)) {
//                    during += ClassInfo.DURING_SEP;
                }
                during += start.getText().toString() + " - " + end.getText().toString();
            }

            String time = mTimeStart.getText().toString() + " - " + mTimeEnd.getText().toString();
            if (!REHelper.isEmpty(name) && !REHelper.isEmpty(during) && !REHelper.isEmpty(time)) {
                String type = mType.getText().toString();
                String teacher = mTeacher.getText().toString();
                String place = mClassPlace.getText().toString();
//                return new ClassInfo(name, type, teacher, place);
            } else {
                Toast.makeText(mName.getContext(), "请输入课程名称, 上课时间, 课程周期\n(这些都很重要)", Toast.LENGTH_LONG).show();
            }
            return null;
        }

        void addDuringEditor(int startWeek, int endWeek) {
            final LinearLayout linearLayout = new LinearLayout(mDuringLayout.getContext());
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            final MaterialEditText start = new MaterialEditText(mDuringLayout.getContext());
            start.setText(startWeek + "");
            start.setHint(R.string.start);
            start.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NORMAL);
            start.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

            TextView to = new TextView(mDuringLayout.getContext());
            to.setText(R.string.to);
            to.setPadding(10, 0, 10, 0);

            final MaterialEditText end = new MaterialEditText(mDuringLayout.getContext());
            end.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NORMAL);
            end.setHint(R.string.end);
            end.setText(endWeek + "");
            end.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

            TextView del = new TextView(mDuringLayout.getContext());
            del.setText(R.string.delete);
            del.setPadding(10, 0, 0, 0);
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDuringMap.size() > 1) {
                        mDuringMap.remove(start);
                        mDuringLayout.removeView(linearLayout);
                        mContext.classInfoModified();
                        Snackbar.make(mDuringLayout, "已删除周期", BaseTransientBottomBar.LENGTH_INDEFINITE)
                                .setAction(android.R.string.cancel, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        mDuringMap.put(start, end);
                                        mDuringLayout.addView(linearLayout);
                                        mContext.classInfoModified();
                                    }
                                }).show();
                    } else {
                        Toast.makeText(mDuringLayout.getContext(), "必须保留至少一个周期~", Toast.LENGTH_LONG).show();
                    }
                }
            });

            linearLayout.addView(start);
            linearLayout.addView(to);
            linearLayout.addView(end);
            linearLayout.addView(del);

            LinearLayout.LayoutParams startParams = (LinearLayout.LayoutParams) start.getLayoutParams();
            startParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            startParams.weight = 2;
            startParams.width = 0;

            LinearLayout.LayoutParams toParams = (LinearLayout.LayoutParams) to.getLayoutParams();
            toParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            toParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;

            LinearLayout.LayoutParams endParams = (LinearLayout.LayoutParams) end.getLayoutParams();
            endParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            endParams.weight = 2;
            endParams.width = 0;

            LinearLayout.LayoutParams delParams = (LinearLayout.LayoutParams) to.getLayoutParams();
            delParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            delParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;

            mDuringLayout.addView(linearLayout);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) linearLayout.getLayoutParams();
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            params.width = LinearLayout.LayoutParams.MATCH_PARENT;

            mDuringMap.put(start, end);
        }
    }
}
