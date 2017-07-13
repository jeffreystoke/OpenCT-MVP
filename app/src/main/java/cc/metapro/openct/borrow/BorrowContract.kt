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

package cc.metapro.openct.borrow

import android.support.v4.app.FragmentManager
import cc.metapro.openct.data.university.model.BorrowInfo
import cc.metapro.openct.utils.base.BaseView
import cc.metapro.openct.utils.base.LoginPresenter

interface BorrowContract {
    interface View : BaseView<Presenter> {
        fun updateBorrows(l: List<BorrowInfo>)
    }

    interface Presenter : LoginPresenter {
        fun loadLocalBorrows()
        fun startFilter(f: FragmentManager)
        fun storeBorrows()
        fun showAll()
        fun showDue()
    }
}