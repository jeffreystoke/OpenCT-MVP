package cc.metapro.openct.data.university.item;

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

import android.graphics.Color;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.REHelper;

@Keep
public class EnrichedClassInfo implements Comparable<EnrichedClassInfo> {

    private String id;
    private ClassInfo mClassInfo;
    private int dailySeq = 1;
    private int dayOfWeek = 1;
    private int color;

    public EnrichedClassInfo() {
        id = UUID.randomUUID().toString();
        color = Color.parseColor("#968bc34a");
    }

    public EnrichedClassInfo(ClassInfo info) {
        this();
        mClassInfo = info;
        dailySeq = REHelper.getStartEnd(info.getTime())[0];
    }

    /**
     * @param classInfo 基础课程信息 - 显示的信息
     * @param dayOfWeek 星期几
     * @param dailySeq  第几节课
     * @param color     背景色
     */
    public EnrichedClassInfo(ClassInfo classInfo, int dayOfWeek, int dailySeq, int color) {
        id = UUID.randomUUID().toString();
        mClassInfo = classInfo;

        this.dailySeq = dailySeq;
        this.dayOfWeek = dayOfWeek;
        this.color = color;
    }

    @NonNull
    public List<ClassInfo> getAllClasses() {
        if (isEmpty()) {
            return new ArrayList<>(0);
        }
        List<ClassInfo> list = new ArrayList<>();
        list.add(mClassInfo);
        ClassInfo c = mClassInfo;
        while (c.hasSubClass()) {
            c = c.getSubClassInfo();
            list.add(c);
        }
        return list;
    }

    public ClassInfo getFirstClassInfo() {
        return mClassInfo;
    }

    public void addClassInfo(ClassInfo info) {
        if (isEmpty()) {
            return;
        }
        if (mClassInfo.hasSubClass()) {
            ClassInfo sub = mClassInfo.getSubClassInfo();
            while (sub.hasSubClass()) {
                sub = sub.getSubClassInfo();
            }
            sub.setSubClassInfo(info);
        } else {
            mClassInfo.setSubClassInfo(info);
        }
    }

    public String getId() {
        return id;
    }


    public void addViewTo(ViewGroup viewGroup, final AppCompatActivity context, int week) {
        if (isEmpty()) {
            return;
        }

        ClassInfo target = null;
        if (week > 0) {
            List<ClassInfo> infoList = getAllClasses();
            for (ClassInfo c : infoList) {
                if (c.hasClass(week)) {
                    target = c;
                }
            }
            if (target == null) {
                return;
            }
        } else {
            target = mClassInfo;
        }

        final CardView card = (CardView) LayoutInflater.from(context).inflate(R.layout.item_class_info, viewGroup, false);
        TextView textView = (TextView) card.findViewById(R.id.class_name);
        int length = target.getLength();
        if (length > 5) {
            length = 1;
        }

        if (length < 1) {
            length = 1;
            textView.setText(target.getName());
        } else {
            textView.setText(target.getName() + "@" + target.getPlace());
        }

        card.setCardBackgroundColor(color);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClassDetailActivity.actionStart(context, EnrichedClassInfo.this);
            }
        });

        card.setX((dayOfWeek - 1) * Constants.CLASS_WIDTH);
        card.setY((dailySeq - 1) * Constants.CLASS_BASE_HEIGHT);

        viewGroup.addView(card);
        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.width = Constants.CLASS_WIDTH;
        params.height = length * Constants.CLASS_BASE_HEIGHT;
    }

    public boolean isToday() {
        Calendar calendar = Calendar.getInstance();
        return weekDayTrans(dayOfWeek) == calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getWeekDay() {
        return weekDayTrans(dayOfWeek);
    }

    private int weekDayTrans(int i) {
        if (i > 0 && i < 8) {
            switch (i) {
                case 1:
                    return Calendar.MONDAY;
                case 2:
                    return Calendar.TUESDAY;
                case 3:
                    return Calendar.WEDNESDAY;
                case 4:
                    return Calendar.THURSDAY;
                case 5:
                    return Calendar.FRIDAY;
                case 6:
                    return Calendar.SATURDAY;
                case 7:
                    return Calendar.SUNDAY;
            }
        }
        return -1;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setClassInfo(ClassInfo info) {
        mClassInfo = info;
        if (info != null) {
            dailySeq = REHelper.getStartEnd(info.getTime())[0];
        }
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : StoreHelper.toJson(this);
    }

    public boolean isEmpty() {
        return mClassInfo == null;
    }

    public boolean equalsCoordinate(EnrichedClassInfo info) {
        return dayOfWeek == info.dayOfWeek && dailySeq == info.dailySeq;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof EnrichedClassInfo) {
            EnrichedClassInfo classInfo = (EnrichedClassInfo) obj;
            return !TextUtils.isEmpty(id) && id.equals(classInfo.id);
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull EnrichedClassInfo o) {
        if (dailySeq <= o.dailySeq) {
            return 1;
        } else {
            return -1;
        }
    }

}
