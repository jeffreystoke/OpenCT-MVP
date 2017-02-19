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

import android.graphics.Color;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.classdetail.ClassDetailActivity;
import cc.metapro.openct.data.source.StoreHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.ClassInfoHelper;
import cc.metapro.openct.utils.Constants;

/**
 * One EnrichedClassInfo Object has one ClassInfo Object
 */
@Keep
public class EnrichedClassInfo implements Comparable<EnrichedClassInfo> {

    private String name;

    private String type;

    private Map<ClassTime, ClassInfo> mTimeMap = new HashMap<>();

    private int color = Color.parseColor("#968bc34a");

    public EnrichedClassInfo(String content, int weekday, CmsFactory.ClassTableInfo info) {
        String[] tmp = content.split(HTMLUtils.BR_REPLACER);
        name = ClassInfoHelper.infoParser(info.mNameIndex, info.mNameRE, tmp);
        type = ClassInfoHelper.infoParser(info.mTypeIndex, info.mTypeRE, tmp);

        ClassTime time = new ClassTime(tmp[info.mTimeIndex], weekday);
        ClassInfo classInfo = new ClassInfo(content, info);
        mTimeMap.put(time, classInfo);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @NonNull
    public List<ClassTime> hasClassThisWeek(int week) {
        List<ClassTime> result = new ArrayList<>();
        for (ClassTime time : mTimeMap.keySet()) {
            ClassInfo info = mTimeMap.get(time);
            Set<ClassDuring> duringList = info.getDuringSet();
            if (duringList != null) {
                for (ClassDuring during : duringList) {
                    if (during.hasClass(week)) {
                        result.add(time);
                    }
                }
            }
        }
        return result;
    }

    @NonNull
    public List<ClassTime> hasClassToday(int week) {
        Calendar calendar = Calendar.getInstance();
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        List<ClassTime> timeList = hasClassThisWeek(week);
        List<ClassTime> result = new ArrayList<>();
        for (ClassTime time : timeList) {
            if (time.inSameDay(dayOfWeek)) {
                result.add(time);
            }
        }
        return result;
    }

    public void combine(EnrichedClassInfo info) {
        if (info == null) return;
        mTimeMap.putAll(info.getTimeMap());
    }

    public void addViewTo(GridLayout gridLayout, final LayoutInflater inflater, int week) {
        if (isEmpty()) return;

        List<ClassTime> timeList = hasClassThisWeek(week);
        if (week > 0 && timeList.isEmpty()) {
            return;
        } else if (week < 0) {
            timeList = new ArrayList<>();
            for (ClassTime time : mTimeMap.keySet()) {
                timeList.add(time);
            }
        }

        for (ClassTime time : timeList) {
            final CardView card = (CardView) inflater.inflate(R.layout.item_class_info, gridLayout, false);
            TextView textView = (TextView) card.findViewById(R.id.class_name);
            int length = time.getLength();
            if (length > 5 || length < 1) {
                length = 1;
                textView.setText(name);
            } else {
                for (ClassInfo info : mTimeMap.values()) {
                    textView.setText(name + "@" + info.getPlace());
                }
            }

            card.setCardBackgroundColor(color);
            card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClassDetailActivity.actionStart(inflater.getContext(), EnrichedClassInfo.this);
                }
            });

            card.setX((time.getWeekDay() - 1) * Constants.CLASS_WIDTH);
            card.setY((time.getDailySeq() - 1) * Constants.CLASS_BASE_HEIGHT);

            gridLayout.addView(card);
            ViewGroup.LayoutParams params = card.getLayoutParams();
            params.width = Constants.CLASS_WIDTH;
            params.height = length * Constants.CLASS_BASE_HEIGHT;
        }
    }

    public boolean isEmpty() {
        return mTimeMap == null || mTimeMap.isEmpty();
    }

    public Map<ClassTime, ClassInfo> getTimeMap() {
        return mTimeMap;
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : StoreHelper.toJson(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof EnrichedClassInfo) {
            EnrichedClassInfo classInfo = (EnrichedClassInfo) obj;
            return !TextUtils.isEmpty(name) && name.equals(classInfo.name);
        }
        return false;
    }

    @Override
    public int compareTo(@NonNull EnrichedClassInfo o) {
        for (ClassTime time : mTimeMap.keySet()) {
            for (ClassTime time1 : o.mTimeMap.keySet()) {
                if (time.inSameDay(time1) && time.getDailySeq() <= time1.getDailySeq()) {
                    return -1;
                }
            }
        }
        return 1;
    }

}
