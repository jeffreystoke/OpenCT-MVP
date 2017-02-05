package cc.metapro.openct.classdetail;

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

import java.util.List;

import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.widget.DailyClassWidget;

class ClassDetailPresenter implements ClassDetailContract.Presenter {

    private List<EnrichedClassInfo> mEnrichedClasses;

    private Context mContext;

    ClassDetailPresenter(Context context, ClassDetailContract.View view) {
        mContext = context;
        DBManger manger = DBManger.getInstance(mContext);
        mEnrichedClasses = manger.getClasses();
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void storeClassInfo(EnrichedClassInfo info) {
        boolean found = false;
        for (EnrichedClassInfo enrichedClassInfo : mEnrichedClasses) {
            // 找到修改目标
            if (enrichedClassInfo.equals(info)) {
                found = true;
                mEnrichedClasses.remove(enrichedClassInfo);
                mEnrichedClasses.add(info);
                break;
            }
        }

        if (!found) {
            found = false;
            for (EnrichedClassInfo enrichedClassInfo : mEnrichedClasses) {
                if (enrichedClassInfo.equalsCoordinate(info)) {
                    found = true;
                    mEnrichedClasses.remove(enrichedClassInfo);
                    enrichedClassInfo.addClassInfo(info.getFirstClassInfo());
                    mEnrichedClasses.add(enrichedClassInfo);
                    break;
                }
            }
            if (!found) {
                mEnrichedClasses.add(info);
            }
        }

        DBManger manger = DBManger.getInstance(mContext);
        manger.updateClasses(mEnrichedClasses);
        DailyClassWidget.update(mContext);
    }
}
