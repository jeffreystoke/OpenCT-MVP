package cc.metapro.openct.data.university.item;

/*
 *  Copyright 2016 - 2017 metapro.cc Jeffctor
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.homepage.ClassContract;

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
     * 所有课程信息组件的基础长度, 基础宽度, 最大高度
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
     * @param classInfo 课程信息
     * @param x         坐标 x
     * @param y         坐标 y
     * @param color     背景色
     * @param width     基础宽度
     * @param height    基础高度
     * @param dayOfWeek 星期几
     */
    public EnrichedClassInfo(ClassInfo classInfo, int x, int y, int color, int width, int height, int dayOfWeek) {
        mClassInfo = classInfo;
        this.x = x;
        this.y = y;
        this.color = color;
        this.width = width;
        this.height = height;
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

    /**
     * 生成课程信息的视图
     *
     * @param context   用于生成 CardView
     * @param presenter 用于删除课程
     * @param week      第几周 (<= 0 表示不比较周数, 直接添加)
     */
    public void addViewTo(ViewGroup viewGroup,
                          final Context context,
                          final ClassContract.Presenter presenter,
                          int week) {
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
        textView.setText(target.getName() + "@" + target.getPlace());

        card.setX(x);
        card.setY(y);
        card.setCardBackgroundColor(color);

        final AlertDialog dialog = target.getAlertDialog(context, presenter);
        card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        viewGroup.addView(card);
        ViewGroup.LayoutParams params = card.getLayoutParams();
        params.width = width;
        params.height = height;
    }

    public ClassInfo getClassInfo() {
        return mClassInfo;
    }

    public boolean isToday() {
        Calendar calendar = Calendar.getInstance();
        return weekDaySwitch(mDayOfWeek) == calendar.get(Calendar.DAY_OF_WEEK);
    }

    private int weekDaySwitch(int i) {
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


    @Override
    public String toString() {
        return StoreHelper.getJsonText(this);
    }
}
