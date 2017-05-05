package cc.metapro.openct.myclass;

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
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.classinfo.Classes;
import cc.metapro.openct.data.university.model.classinfo.SingleClass;

class DailyAdapter extends RecyclerView.Adapter<DailyAdapter.ClassViewHolder> {

    @NonNull
    private List<SingleClass> mTodayClasses = new ArrayList<>(0);

    private LayoutInflater mInflater;

    DailyAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        holder.setInfo(mTodayClasses.get(position));
    }

    @Override
    public int getItemCount() {
        return mTodayClasses.size();
    }

    void updateTodayClasses(Classes classes, int week) {
        mTodayClasses = classes.getTodayClasses(week);
    }

    boolean hasClassToday() {
        return mTodayClasses.size() > 0;
    }

    static class ClassViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.class_name)
        TextView mClassName;
        @BindView(R.id.class_place_time)
        TextView mTimePlace;

        ClassViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void setInfo(SingleClass info) {
            mClassName.setText(info.getName());
            String content = "";

            if (!TextUtils.isEmpty(info.getTimeString())) {
                content += mClassName.getContext().getString(R.string.text_today_seq, info.getTimeString());
            }

            if (!TextUtils.isEmpty(info.getPlace())) {
                if (!TextUtils.isEmpty(content)) {
                    content += ", ";
                }
                content += mClassName.getContext().getString(R.string.text_place_at, info.getPlace());
            }

            mTimePlace.setText(content);
        }
    }
}
