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
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.interactiveweb.InteractiveWebViewClient;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.item.BorrowInfo;
import cc.metapro.openct.data.university.item.EnrichedClassInfo;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.MyObserver;
import cc.metapro.openct.utils.webutils.TableUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static cc.metapro.interactiveweb.InteractiveWebView.CLICK_FLAG;

public class CustomPresenter implements CustomContract.Presenter {

    private static final String TAG = CustomPresenter.class.getSimpleName();
    private CustomContract.View mView;
    private Context mContext;
    private String USERNAME;
    private String PASSWORD;
    private int CMD_INDEX;
    private List<Map<Element, String>> commands;
    private Observer<Map<Element, String>> mObserver;
    private WebConfiguration mConfiguration;
    private String TYPE;


    CustomPresenter(Context context, CustomContract.View view, String type) {
        mView = view;
        mContext = context;
        mView.setPresenter(this);
        TYPE = type;
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

        if (mConfiguration == null) {
//            Toast.makeText(mContext, "还没有进行过定制, 请按提示进行定制", Toast.LENGTH_LONG).show();
            mConfiguration = new WebConfiguration();
        }
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

    @Override
    public void execCommands(final InteractiveWebView mWebView) {
        CMD_INDEX = 0;
        commands = new ArrayList<>();
        Observable<Map<Element, String>> observable =
                Observable.create(new ObservableOnSubscribe<Map<Element, String>>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Map<Element, String>> e) throws Exception {
                        final LinkedHashMap<Element, String> map = mConfiguration.getClickedCommands();
                        Map<Element, String> tmpMap = new HashMap<>();
                        for (final Element element : map.keySet()) {
                            final String value = map.get(element);
                            if (HTMLUtils.isClickable(element)) {
                                commands.add(tmpMap);
                                commands.add(new HashMap<Element, String>() {{
                                    put(element, value);
                                }});
                                tmpMap = new HashMap<>();
                                commands.add(tmpMap);
                            } else {
                                tmpMap.put(element, value);
                            }
                        }
                    }
                });

        mObserver = new MyObserver<Map<Element, String>>(TAG) {
            @Override
            public void onSubscribe(Disposable d) {
                super.onSubscribe(d);
                mWebView.setPageFinishCallback(new InteractiveWebViewClient.FinishCallBack() {
                    @Override
                    public void onPageFinish() {
                        if (CMD_INDEX < commands.size()) {
                            onNext(commands.get(CMD_INDEX++));
                            mWebView.setPageFinishCallback(new InteractiveWebViewClient.FinishCallBack() {
                                @Override
                                public void onPageFinish() {
                                    mView.enableNextStep();
                                }
                            });
                            mWebView.setOnStartCallBack(new InteractiveWebViewClient.StartCallBack() {
                                @Override
                                public void onPageStart() {
                                    mView.disableNextStep();
                                }
                            });
                        }
                    }
                });

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(mView.getUrl());
                    }
                });
            }

            @Override
            public void onNext(Map<Element, String> map) {
                for (Element e : map.keySet()) {
                    String command = map.get(e);
                    switch (command) {
                        case CLICK_FLAG:
                            if (!mWebView.clickElementById(e.id())) {
                                if (!mWebView.clickElementsByName(e.attr("name"))) {
                                    if (!mWebView.clickElementsByPattern(e.html())) {
                                        mWebView.clickElementsByTag(e.tagName());
                                    }
                                }
                            }
                            break;
                        default:
                            if (!mWebView.setById(e.id(), "value", map.get(e))) {
                                if (!mWebView.setByName(e.attr("name"), "value", map.get(e))) {
                                    if (!mWebView.setByPattern(e.toString(), "value", map.get(e))) {
                                        mWebView.setByTag(e.tagName(), "value", map.get(e));
                                    }
                                }
                            }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
            }
        };

        observable.subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    @Override
    public void nextStep(final InteractiveWebView mWebView, final FragmentManager manager) {
        if (mObserver != null) {
            if (CMD_INDEX < commands.size()) {
                mObserver.onNext(commands.get(CMD_INDEX++));
            } else {
                Toast.makeText(mContext, "已经到达之前设置的终点了", Toast.LENGTH_SHORT).show();
                mObserver.onComplete();
                switch (TYPE) {
                    case Constants.TYPE_CLASS:
                        if (!TextUtils.isEmpty(Constants.advCustomInfo.mClassTableInfo.mClassTableID)) {
                            List<Element> rawClasses = UniversityUtils.getRawClasses(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(Constants.advCustomInfo.mClassTableInfo.mClassTableID), mContext);
                            List<EnrichedClassInfo> classInfoList = UniversityUtils
                                    .generateClasses(mContext, rawClasses, Constants.advCustomInfo.mClassTableInfo);
                            DBManger.getInstance(mContext).updateClasses(classInfoList);
                            Toast.makeText(mContext, R.string.custom_finish_tip, Toast.LENGTH_LONG).show();
                        }
                        return;
                    case Constants.TYPE_GRADE:
                        if (!TextUtils.isEmpty(Constants.advCustomInfo.GRADE_TABLE_ID)) {
                            List<GradeInfo> grades = UniversityUtils.generateInfo(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(Constants.advCustomInfo.GRADE_TABLE_ID), GradeInfo.class);
                            DBManger.getInstance(mContext).updateGrades(grades);
                            Toast.makeText(mContext, "获取成绩信息成功, 请回到成绩信息界面查看", Toast.LENGTH_LONG).show();
                        }
                        return;
                    case Constants.TYPE_SEARCH:
                        return;
                    case Constants.TYPE_BORROW:
                        if (!TextUtils.isEmpty(Constants.advCustomInfo.BORROW_TABLE_ID)) {
                            List<BorrowInfo> borrows = UniversityUtils.generateInfo(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(Constants.advCustomInfo.BORROW_TABLE_ID), BorrowInfo.class);
                            DBManger.getInstance(mContext).updateBorrows(borrows);
                            Toast.makeText(mContext, "获取借阅信息成功, 请回到借阅信息界面查看", Toast.LENGTH_LONG).show();
                        }
                        return;
                }
                ActivityUtils.showTableChooseDialog(manager, TYPE, mWebView.getPageDom(), null);
            }
        }
    }

}
