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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.utils.Constants;

public class SingleClass implements Comparable<SingleClass>, View.OnClickListener {

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

    /**
     * Get Time in String, actually it is sequence of class daily
     * for example 3 - 4 or 3
     */
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

    public void addViewTo(final ViewGroup gridLayout, LayoutInflater inflater) {
        int x = (time.getWeekDay() - 1) * Constants.CLASS_WIDTH;
        int y = (time.getDailySeq() - 1) * Constants.CLASS_BASE_HEIGHT;
        int N = gridLayout.getChildCount();
        for (int i = 0; i < N; i++) {
            int childX = (int) gridLayout.getChildAt(i).getX();
            int childY = (int) gridLayout.getChildAt(i).getY();
            if (childX == x && childY == y) {
                return;
            }
        }

        final CardView card = (CardView) inflater.inflate(R.layout.item_class_info, gridLayout, false);
        card.setCardBackgroundColor(color);

        TextView textView = (TextView) card.findViewById(R.id.class_name);
        int length = time.getDailyEnd() - time.getDailySeq() + 1;
        if (length > 5 || length < 1) {
            length = 1;
            textView.setText(name);
        } else {
            textView.setText(name + "@" + time.getPlace());
        }

        card.setX(x);
        card.setY(y);
        gridLayout.addView(card);
        card.getLayoutParams().height = length * Constants.CLASS_BASE_HEIGHT;
        card.getLayoutParams().width = Constants.CLASS_WIDTH;
        card.setOnClickListener(this);
    }

    @Override
    public int compareTo(@NonNull SingleClass o) {
        return time.compareTo(o.time);
    }

    @Override
    public void onClick(View v) {
        ClassDetailActivity.actionStart(v.getContext(), name);
    }
}
