package cc.metapro.openct.search;

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

import cc.metapro.openct.data.university.model.BookInfo;
import cc.metapro.openct.utils.base.BasePresenter;
import cc.metapro.openct.utils.base.BaseView;
import io.reactivex.disposables.Disposable;

@Keep
interface LibSearchContract {

    interface View extends BaseView<Presenter> {

        void showOnSearching();

        void onSearchResult(List<BookInfo> books);

        void onNextPageResult(List<BookInfo> books);

    }

    interface Presenter extends BasePresenter {

        Disposable search(final String type, final String content);

        Disposable nextPage();

    }
}
