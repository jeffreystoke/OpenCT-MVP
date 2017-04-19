package cc.metapro.openct.custom;

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

import android.support.v4.app.FragmentManager;

import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.openct.utils.base.BasePresenter;
import cc.metapro.openct.utils.base.BaseView;

interface CustomContract {

    interface View extends BaseView<Presenter> {
    }

    interface Presenter extends BasePresenter {
        void setWebView(final InteractiveWebView webView, final FragmentManager manager);
    }
}
