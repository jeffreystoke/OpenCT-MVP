package cc.metapro.openct.data.university.item.classinfo;

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

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cc.metapro.openct.R;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.GridLayoutHelper;

public class SingleClass implements Comparable<SingleClass> {

    private String name;
    private String type;
    private ClassTime time;
    private String place;
    private String teacher;
    private int color;

    SingleClass(String name, String type, ClassTime time, String place, String teacher, int color) {
        this.name = name;
        this.type = type;
        this.time = time;
        this.place = place;
        this.teacher = teacher;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTimeString() {
        return time.getTime();
    }

    public ClassTime getClassTime() {
        return time;
    }

    public String getPlace() {
        return place;
    }

    public String getTeacher() {
        return teacher;
    }

    public void addViewTo(GridLayout gridLayout, LayoutInflater inflater) {
        final CardView card = (CardView) inflater.inflate(R.layout.item_class_info, gridLayout, false);
        TextView textView = (TextView) card.findViewById(R.id.class_name);
        int length = time.getLength();
        if (length > 5 || length < 1) {
            length = 1;
            textView.setText(name);
        } else {
            textView.setText(name + "@" + time.getPlace());
        }

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        //设置所在列的宽度和高度
        params.width = Constants.CLASS_WIDTH;
        params.height = length * Constants.CLASS_BASE_HEIGHT;
        int row = time.getDailySeq() - 1;
        int col = time.getWeekDay() - 1;
        params.rowSpec = GridLayout.spec(row);
        params.columnSpec = GridLayout.spec(col);

        card.setCardBackgroundColor(color);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 17/2/22  go to class detail activity
            }
        });

        if (row > gridLayout.getRowCount()) {
            gridLayout.setRowCount(row + length + 1);
            GridLayoutHelper.fillGrids(gridLayout);
        }

        gridLayout.addView(card, params);
    }

    @Override
    public int compareTo(@NonNull SingleClass o) {
        return time.compareTo(o.time);
    }
}
