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

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.homepage.ClassContract;
import cc.metapro.openct.utils.Constants;

@Keep
public class EnrichedClassInfo {

    /**
     * 课程信息
     */
    private ClassInfo mClassInfo;

    /**
     * 在 ScrollView 中的坐标
     */
    private int x, y;

    /**
     * 所有课程信息组件的基础长度, 宽度, 高度
     */
    private int width, height;

    /**
     * 背景色
     */
    private int color;

    /**
     * 星期几
     */
    private int mDayOfWeek;

    /**
     * 用于用户创建课程信息
     *
     * @param classInfo 基础课程信息 - 显示的信息
     * @param dayOfWeek 星期几
     * @param dailySeq  第几节课
     * @param color     背景色
     */
    public EnrichedClassInfo(ClassInfo classInfo, int dayOfWeek, int dailySeq, int color) {
        mClassInfo = classInfo;
        this.x = Constants.CLASS_WIDTH * (dayOfWeek - 1);
        this.y = Constants.CLASS_BASE_HEIGHT * (dailySeq - 1);
        this.color = color;
        this.width = Constants.CLASS_WIDTH;
        this.height = Constants.CLASS_BASE_HEIGHT * Constants.CLASS_LENGTH;

        mDayOfWeek = dayOfWeek;
    }

    /**
     * 获取所有的课程信息 (课程和子课程 - 在同一个格子中包含的课程信息)
     *
     * @return a list of class info
     */
    @NonNull
    public List<ClassInfo> getAllClasses() {
        List<ClassInfo> list = new ArrayList<>();
        list.add(mClassInfo);
        ClassInfo c = mClassInfo;
        while (c.hasSubClass()) {
            c = c.getSubClassInfo();
            list.add(c);
        }
        return list;
    }


    public ClassInfo
    getFirstClassInfo() {
        return mClassInfo;
    }

    public void
    addClassInfo(ClassInfo info) {
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

    /**
     * 生成课程信息的视图
     *
     * @param context   用于生成 CardView
     * @param presenter 用于删除课程
     * @param week      第几周 (<= 0 表示不比较周数, 直接添加)
     */
    public void addViewTo(ViewGroup viewGroup, final FragmentActivity context, final ClassContract.Presenter presenter, int week) {
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
        final CardView card = (CardView) LayoutInflater.from(context).inflate(R.layout.item_class_info, null);
        TextView textView = (TextView) card.findViewById(R.id.class_name);
        int length = target.getLength();
        if (length >= 5) {
            length = 1;
        }

        if (length <= 1) {
            length = 1;
            textView.setText(target.getName());
        } else {
            textView.setText(target.getName() + "@" + target.getPlace());
        }

        card.setX(x);
        card.setY(y);
        card.setCardBackgroundColor(color);

        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClassDetailActivity.actionStart(context, EnrichedClassInfo.this);
            }
        });
        viewGroup.addView(card);
        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.width = width;
        params.height = length * Constants.CLASS_BASE_HEIGHT;
    }

    public boolean isToday() {
        Calendar calendar = Calendar.getInstance();
        return weekDayTrans(mDayOfWeek) == calendar.get(Calendar.DAY_OF_WEEK);
    }

    public int getDayOfWeek() {
        return mDayOfWeek;
    }

    public int getWeekDay() {
        return weekDayTrans(mDayOfWeek);
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

    public void replaceClassInfo(ClassInfo oldInfo, ClassInfo newInfo) {
        if (mClassInfo.equals(oldInfo)) {
            newInfo.setSubClassInfo(mClassInfo.getSubClassInfo());
            mClassInfo = newInfo;
        } else if (mClassInfo.hasSubClass()) {
            ClassInfo parent = mClassInfo;
            while (parent.hasSubClass() && !parent.getSubClassInfo().equals(oldInfo)) {
                parent = parent.getSubClassInfo();
            }
            newInfo.setSubClassInfo(parent.getSubClassInfo().getSubClassInfo());
            parent.setSubClassInfo(newInfo);
        }
    }

    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }

    /**
     * 拥有相同的坐标即为相等
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof EnrichedClassInfo) {
            EnrichedClassInfo classInfo = (EnrichedClassInfo) obj;
            if (classInfo.getFirstClassInfo().equals(mClassInfo)) {
                return true;
            }
        }
        return false;
    }
}
