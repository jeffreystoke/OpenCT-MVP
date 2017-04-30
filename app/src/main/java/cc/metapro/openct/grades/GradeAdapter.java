package cc.metapro.openct.grades;

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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.university.model.GradeInfo;

@Keep
class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<GradeInfo> mGrades;

    private LayoutInflater mInflater;

    GradeAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mGrades = new ArrayList<>(0);
    }

    @Override
    public GradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GradeViewHolder holder, int position) {
        final GradeInfo g = mGrades.get(position);
        holder.setInfo(g);
    }

    @Override
    public int getItemCount() {
        return mGrades.size();
    }

    void updateGrades(@NonNull List<GradeInfo> grades) {
        mGrades = grades;
    }

    class GradeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.grade_class_name)
        TextView mClassName;

        GradeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setInfo(GradeInfo info) {
            mClassName.setText(info.toFullString());
        }
    }
}
