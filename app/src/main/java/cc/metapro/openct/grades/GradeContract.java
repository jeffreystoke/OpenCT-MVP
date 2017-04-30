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

import android.support.annotation.Keep;

import java.util.List;
import java.util.Map;

import cc.metapro.openct.data.university.model.GradeInfo;
import cc.metapro.openct.utils.base.BaseView;
import cc.metapro.openct.utils.base.LoginPresenter;

@Keep
public interface GradeContract {

    interface View extends BaseView<Presenter> {

        void onLoadGrades(List<GradeInfo> grades);

        void showCETDialog();

        void onLoadCETGrade(Map<String, String> resultMap);

    }

    interface Presenter extends LoginPresenter {

        void loadLocalGrades();

        void loadCETGrade(Map<String, String> queryMap);

        void storeGrades();

        void clearGrades();
    }
}
