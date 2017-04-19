package cc.metapro.openct.utils.base;

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

import java.util.Map;

import io.reactivex.disposables.Disposable;

public interface LoginPresenter extends BasePresenter {

    /**
     * 加载网页, 判断是否需要验证码, 需要时弹出验证码输入框, 不需要时 loadUserCenter
     *
     * @param manager supportFragmentManager for CaptchaDialog
     */
    Disposable loadOnlineInfo(final FragmentManager manager);

    /**
     * 登录获取用户中心, 并在用户中心获得链接, 并显示在对话框中(未存储)
     *
     * @param manager supportFragmentManager for LinkSelectionDialog
     * @param code    captcha code
     */
    Disposable loadUserCenter(final FragmentManager manager, final String code);

    /**
     * @param manager supportFragmentManager for FormDialog
     * @param url     url selected in LinkSelectionDialog
     */
    Disposable loadTargetPage(final FragmentManager manager, final String url);

    /**
     * @param manager   supportFragmentManager for TableChooseDialog
     * @param actionURL form action absolute url
     * @param queryMap  form key value map
     */
    Disposable loadQuery(final FragmentManager manager, final String actionURL, final Map<String, String> queryMap, final boolean needNewPage);

}
