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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import cc.metapro.openct.data.university.item.GradeInfo;

@Keep
class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

    private List<GradeInfo> mGradeInfos;

    private Context mContext;

    GradeAdapter(Context context) {
        mContext = context;
        mGradeInfos = new ArrayList<>(0);
    }

    @Override
    public GradeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_grade, parent, false);
        return new GradeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GradeViewHolder holder, int position) {
        final GradeInfo g = mGradeInfos.get(position);
        holder.setClassName(g.getClassName());
        holder.setGradeSummary(g.getGradeSummary());
        holder.setListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(mContext);
                ab.setMessage(g.toFullString());
                ab.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGradeInfos.size();
    }

    void updateGradeInfos(List<GradeInfo> gradeInfos) {
        if (gradeInfos == null || gradeInfos.size() == 0) {
            mGradeInfos = new ArrayList<>(0);
        } else {
            mGradeInfos = gradeInfos;
        }
    }

    static class GradeViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.grade_class_name)
        TextView mClassName;

        @BindView(R.id.grade_level)
        TextView mGradeSummary;

        GradeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setClassName(String name) {
            mClassName.setText(name);
        }

        void setGradeSummary(String grade) {
            mGradeSummary.setText("总评成绩  " + grade);
            try {
                int i = Integer.parseInt(grade);
                if (i < 60) {
                    mGradeSummary.setTextColor(ContextCompat.getColor(mGradeSummary.getContext(), R.color.colorAccent));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setListener(View.OnClickListener l) {
            mClassName.setOnClickListener(l);
            mGradeSummary.setOnClickListener(l);
        }
    }
}
