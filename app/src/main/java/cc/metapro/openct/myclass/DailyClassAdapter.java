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
import android.support.annotation.Keep;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.item.ClassInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;

@Keep
class DailyClassAdapter extends RecyclerView.Adapter<DailyClassAdapter.ClassViewHolder> {

    private List<ClassInfo> mClasses;

    private Context mContext;

    DailyClassAdapter(Context context) {
        mContext = context;
        mClasses = new ArrayList<>(0);
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_class, parent, false);
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        holder.setInfo(mClasses.get(position));
    }

    @Override
    public int getItemCount() {
        return mClasses.size();
    }

    void updateTodayClasses(List<EnrichedClassInfo> classes, int week) {
        mClasses = new ArrayList<>(0);
        if (classes != null && classes.size() != 0) {
            for (EnrichedClassInfo info : classes) {
                if (info.isToday()) {
                    List<ClassInfo> classList = info.getAllClasses();
                    for (ClassInfo c : classList) {
                        if (c.hasClass(week)) {
                            mClasses.add(c);
                        }
                    }
                }
            }
        }
        Collections.sort(mClasses);
    }

    boolean hasClassToday() {
        return mClasses != null && mClasses.size() > 0;
    }

    class ClassViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.class_name)
        TextView mClassName;

        @BindView(R.id.class_place_time)
        TextView mTimePlace;

        ClassViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setInfo(ClassInfo info) {
            mClassName.setText(info.getName());
            String content = "";
            if (!TextUtils.isEmpty(info.getTime())) content += "今天 " + info.getTime() + " 节 ";
            if (!TextUtils.isEmpty(info.getPlace())) content += "在 " + info.getPlace();
            mTimePlace.setText(content);
        }
    }

}
