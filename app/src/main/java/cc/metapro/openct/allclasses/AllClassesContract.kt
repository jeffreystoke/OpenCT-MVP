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

package cc.metapro.openct.allclasses

import android.support.v4.app.FragmentManager
import cc.metapro.openct.data.university.model.classinfo.Classes
import cc.metapro.openct.utils.base.BasePresenter
import cc.metapro.openct.utils.base.BaseView

interface AllClassesContract {

    interface View : BaseView<Presenter> {
        // 更新所有课程信息
        fun updateClasses()
    }

    interface Presenter : BasePresenter {
        // 导出课程到 iCal
        fun exportClasses()
        // 清除所有课程
        fun clearClasses()
        // 从 Excel 导入
        fun loadFromExcel(f : FragmentManager)
        // 保存课程信息
        fun storeClasses(c : Classes)
    }
}