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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

import org.jsoup.nodes.Element;

import java.util.Map;

import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.utils.Constants;

import static cc.metapro.interactiveweb.InteractiveWebView.CLICK_FLAG;

class CustomPresenter implements CustomContract.Presenter {

    private Context mContext;
    private String USERNAME;
    private String PASSWORD;
    private WebConfiguration mConfiguration;
    private String TYPE;

    CustomPresenter(Context context, CustomContract.View view, String type) {
        mContext = context;
        TYPE = type;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        Constants.advCustomInfo = DBManger.getAdvancedCustomInfo(mContext);
        Map<String, String> userPassMap = null;
        switch (TYPE) {
            case Constants.TYPE_CLASS:
                userPassMap = Loader.getCmsStuInfo(mContext);
                mConfiguration = Constants.advCustomInfo.mClassWebConfig;
                break;
            case Constants.TYPE_GRADE:
                userPassMap = Loader.getCmsStuInfo(mContext);
                mConfiguration = Constants.advCustomInfo.mGradeWebConfig;
                break;
            case Constants.TYPE_BORROW:
                userPassMap = Loader.getLibStuInfo(mContext);
                mConfiguration = Constants.advCustomInfo.mBorrowWebConfig;
                break;
        }

        if (userPassMap != null) {
            USERNAME = userPassMap.get(mContext.getString(R.string.key_username));
            PASSWORD = userPassMap.get(mContext.getString(R.string.key_password));
        }

        mConfiguration = new WebConfiguration();
    }

    @Override
    public void setWebView(final InteractiveWebView webView, final FragmentManager manager) {
        webView.setUserClickCallback(new InteractiveWebView.ClickCallback() {
            @Override
            public void onClick(@NonNull final Element element) {
                if (HTMLUtils.isPasswordInput(element)) {
                    mConfiguration.addAction(element, PASSWORD);
                    if (!webView.setById(element.id(), "value", PASSWORD)) {
                        webView.setByName(element.attr("name"), "value", PASSWORD);
                    }
                } else if (HTMLUtils.isTextInput(element)) {
                    ClickDialog.newInstance(new ClickDialog.TypeCallback() {
                        @Override
                        public void onResult(String type) {
                            mConfiguration.addAction(element, type);
                            switch (type) {
                                case InteractiveWebView.COMMON_INPUT_FLAG:
                                    if (!webView.focusById(element.id())) {
                                        webView.focusByName(element.attr("name"));
                                    }
                                    break;
                                case InteractiveWebView.USERNAME_INPUT_FLAG:
                                    mConfiguration.addAction(element, USERNAME);
                                    if (!webView.setById(element.id(), "value", USERNAME)) {
                                        webView.setByName(element.attr("name"), "value", USERNAME);
                                    }
                                    break;
                            }
                        }
                    }).show(manager, "click_dialog");
                } else if (HTMLUtils.isClickable(element)) {
                    mConfiguration.addAction(element, CLICK_FLAG);
                } else {
                    mConfiguration.addAction(element, "");
                }
            }
        });
    }
}
