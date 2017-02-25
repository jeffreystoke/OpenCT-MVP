package cc.metapro.openct.allclasses;

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

import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jrummyapps.android.colorpicker.ColorPickerDialog;
import com.jrummyapps.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.data.university.item.classinfo.ClassTime;
import cc.metapro.openct.data.university.item.classinfo.EnrichedClassInfo;
import cc.metapro.openct.utils.DateHelper;
import cc.metapro.openct.utils.REHelper;

import static cc.metapro.openct.allclasses.AllClassesActivity.allClasses;


class AllClassesAdapter extends RecyclerView.Adapter<AllClassesAdapter.ClassViewHolder> {

    private LayoutInflater mInflater;
    private AppCompatActivity mActivity;

    AllClassesAdapter(AppCompatActivity activity) {
        mInflater = LayoutInflater.from(activity);
        mActivity = activity;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ClassViewHolder(mInflater.inflate(R.layout.item_class_all, parent, false));
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        holder.setInfo(allClasses.get(position), mActivity, position);
    }

    @Override
    public int getItemCount() {
        return allClasses == null ? 0 : allClasses.size();
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.color)
        TextView mColor;

        @BindView(R.id.name)
        TextView mName;

        @BindView(R.id.time)
        TextView mTime;

        @BindView(R.id.edit)
        TextView mEdit;

        ClassViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setInfo(final EnrichedClassInfo info, final AppCompatActivity activity, final int position) {
            mColor.setBackgroundColor(info.getColor());
            mName.setText(info.getName() + "   " + info.getType());
            String time = "";
            List<ClassTime> tmpTimeList = new ArrayList<>(info.getTimeSet());
            Collections.sort(tmpTimeList);
            for (ClassTime t : tmpTimeList) {
                String tmp = DateHelper.weekDayToChinese(t.getWeekDay()) + " " + t.getTime() + " 节, ";
                if (!time.contains(tmp)) {
                    time += tmp;
                }
            }
            if (!REHelper.isEmpty(time)) {
                time = time.substring(0, time.length() - 2);
            }

            mTime.setText(time);
            mColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ColorPickerDialog dialog = ColorPickerDialog.newBuilder().setColor(info.getColor()).create();
                    dialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                        @Override
                        public void onColorSelected(int dialogId, @ColorInt int color) {
                            mColor.setBackgroundColor(color);
                            allClasses.get(position).setColor(color);
                        }

                        @Override
                        public void onDialogDismissed(int dialogId) {

                        }
                    });
                    dialog.show(activity.getFragmentManager(), "color_picker");
                }
            });
            mEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClassDetailActivity.actionStart(mName.getContext(), position);
                }
            });
        }
    }
}
