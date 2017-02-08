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
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cc.metapro.interactiveweb.InteractiveWebView;
import cc.metapro.interactiveweb.InteractiveWebViewClient;
import cc.metapro.interactiveweb.utils.HTMLUtils;
import cc.metapro.openct.R;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.AdvancedCustomInfo;
import cc.metapro.openct.data.university.UniversityUtils;
import cc.metapro.openct.data.university.WebConfiguration;
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

@Keep
public class CustomActivity extends AppCompatActivity {

    public static final String TAG = CustomActivity.class.getSimpleName();

    public static String CUSTOM_TYPE = Constants.TYPE_CLASS;

    public static final int RECORD_MODE = 6;
    public static final int REPLAY_MODE = 7;
    public static int MODE = RECORD_MODE;

    private String USERNAME;
    private String PASSWORD;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.custom_web_view)
    InteractiveWebView mWebView;
    @BindView(R.id.url_input)
    EditText mURL;
    @BindView(R.id.tips)
    TextView tipText;
    @BindView(R.id.web_layout)
    ViewGroup mViewGroup;
    @BindView(R.id.fab_next)
    FloatingActionButton mFab;

    private int CMD_INDEX;
    private List<Map<Element, String>> commands;
    private Observer<Map<Element, String>> mObserver;
    public static AdvancedCustomInfo advCustomInfo;
    private WebConfiguration mConfiguration;

    private InteractiveWebView.ClickCallback mClickCallback = new InteractiveWebView.ClickCallback() {
        @Override
        public void onClick(@NonNull final Element element) {
            if (HTMLUtils.isPasswordInput(element)) {
                mConfiguration.addAction(element, PASSWORD);
                if (!mWebView.setById(element.id(), "value", PASSWORD)) {
                    mWebView.setByName(element.attr("name"), "value", PASSWORD);
                }
            } else if (HTMLUtils.isTextInput(element)) {
                ClickDialog.newInstance(new ClickDialog.TypeCallback() {
                    @Override
                    public void onResult(String type) {
                        mConfiguration.addAction(element, type);
                        switch (type) {
                            case InteractiveWebView.COMMON_INPUT_FLAG:
                                mWebView.removeUserClickCallback();
                                mWebView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWebView.setUserClickCallback(mClickCallback);
                                    }
                                }, 5000);
                                if (!mWebView.clickElementById(element.id())) {
                                    mWebView.clickElementsByName(element.attr("name"));
                                }
                                break;
                            case InteractiveWebView.USERNAME_INPUT_FLAG:
                                mConfiguration.addAction(element, USERNAME);
                                if (!mWebView.setById(element.id(), "value", USERNAME)) {
                                    mWebView.setByName(element.attr("name"), "value", USERNAME);
                                }
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), "click_dialog");
            } else if (HTMLUtils.isClickable(element)) {
                mConfiguration.addAction(element, CLICK_FLAG);
            } else {
                mConfiguration.addAction(element, "");
            }
        }
    };

    @OnClick(R.id.fab)
    public void start() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        MODE = RECORD_MODE;

        mConfiguration = new WebConfiguration();

        mFab.setVisibility(View.GONE);
        mWebView.setUserClickCallback(mClickCallback);
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(url);
    }

    @OnClick(R.id.fab_replay)
    public void replay() {
        String url = URLUtil.guessUrl(mURL.getText().toString());
        mURL.setText(url);
        MODE = REPLAY_MODE;

        mWebView.removeUserClickCallback();
        mFab.setVisibility(View.VISIBLE);
        mObserver = null;
        tipText.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        execCommands(mConfiguration);
    }

    @OnClick(R.id.fab_ok)
    public void showTableChooseDialog() {
        switch (CUSTOM_TYPE) {
            case Constants.TYPE_CLASS:
                advCustomInfo.setClassWebConfig(mConfiguration);
                break;
            case Constants.TYPE_GRADE:
                advCustomInfo.setGradeWebConfig(mConfiguration);
                break;
            case Constants.TYPE_SEARCH:
                break;
            case Constants.TYPE_BORROW:
                advCustomInfo.setBorrowWebConfig(mConfiguration);
                break;
        }
        ActivityUtils.showTableChooseDialog(getSupportFragmentManager(), CUSTOM_TYPE, mWebView.getPageDom(), null);
    }

    @OnClick(R.id.fab_next)
    public void nextStep() {
        if (mObserver != null) {
            if (CMD_INDEX < commands.size()) {
                mObserver.onNext(commands.get(CMD_INDEX++));
            } else {
                Toast.makeText(this, "已经到达之前设置的终点了", Toast.LENGTH_SHORT).show();
                mObserver.onComplete();
                switch (CUSTOM_TYPE) {
                    case Constants.TYPE_CLASS:
                        if (!TextUtils.isEmpty(advCustomInfo.mClassTableInfo.mClassTableID)) {
                            List<Element> rawClasses = UniversityUtils.getRawClasses(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(advCustomInfo.mClassTableInfo.mClassTableID), this);
                            List<EnrichedClassInfo> classInfoList = UniversityUtils
                                    .generateClasses(this, rawClasses, advCustomInfo.mClassTableInfo);
                            DBManger.getInstance(this).updateClasses(classInfoList);
                            Toast.makeText(this, R.string.custom_finish_tip, Toast.LENGTH_LONG).show();
                            finish();
                        }
                        break;
                    case Constants.TYPE_GRADE:
                        if (!TextUtils.isEmpty(advCustomInfo.GRADE_TABLE_ID)) {
                            List<GradeInfo> grades = UniversityUtils.generateInfo(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(advCustomInfo.GRADE_TABLE_ID), GradeInfo.class);
                            DBManger.getInstance(this).updateGrades(grades);
                            Toast.makeText(this, "获取成绩信息成功, 请回到成绩信息界面查看", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        break;
                    case Constants.TYPE_SEARCH:
                        return;
                    case Constants.TYPE_BORROW:
                        if (!TextUtils.isEmpty(advCustomInfo.BORROW_TABLE_ID)) {
                            List<BorrowInfo> borrows = UniversityUtils.generateInfo(TableUtils
                                    .getTablesFromTargetPage(mWebView.getPageDom())
                                    .get(advCustomInfo.BORROW_TABLE_ID), BorrowInfo.class);
                            DBManger.getInstance(this).updateBorrows(borrows);
                            Toast.makeText(this, "获取借阅信息成功, 请回到借阅信息界面查看", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        break;
                }
                ActivityUtils.showTableChooseDialog(getSupportFragmentManager(), CUSTOM_TYPE, mWebView.getPageDom(), null);
            }
        }
    }

    public void execCommands(final WebConfiguration configuration) {
        CMD_INDEX = 0;
        commands = new ArrayList<>();
        Observable<Map<Element, String>> observable =
                Observable.create(new ObservableOnSubscribe<Map<Element, String>>() {
                    @Override
                    public void subscribe(final ObservableEmitter<Map<Element, String>> e) throws Exception {
                        final LinkedHashMap<Element, String> map = configuration.getClickedCommands();
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

        mObserver = new MyObserver<Map<Element, String>>() {
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
                                    mFab.setEnabled(true);
                                }
                            });
                            mWebView.setOnStartCallBack(new InteractiveWebViewClient.StartCallBack() {
                                @Override
                                public void onPageStart() {
                                    mFab.setEnabled(false);
                                }
                            });
                        }
                    }
                });

                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(URLUtil.guessUrl(mURL.getText().toString()));
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
                                    if (!mWebView.setByPattern(HTMLUtils.getElementPattern(e), "value", map.get(e))) {
                                        mWebView.setByTag(e.tagName(), "value", map.get(e));
                                    }
                                }
                            }
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        };

        observable.subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.newThread())
                .subscribe(mObserver);
    }

    public static void actionStart(Context context, String type) {
        CUSTOM_TYPE = type;
        Intent intent = new Intent(context, CustomActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        advCustomInfo = DBManger.getAdvancedCustomInfo(this);
        Map<String, String> userPassMap = null;
        switch (CUSTOM_TYPE) {
            case Constants.TYPE_CLASS:
                userPassMap = Loader.getCmsStuInfo(this);
                mConfiguration = advCustomInfo.mClassWebConfig;
                break;
            case Constants.TYPE_GRADE:
                userPassMap = Loader.getCmsStuInfo(this);
                mConfiguration = advCustomInfo.mGradeWebConfig;
                break;
            case Constants.TYPE_BORROW:
                userPassMap = Loader.getLibStuInfo(this);
                mConfiguration = advCustomInfo.mBorrowWebConfig;
                break;
        }

        if (userPassMap != null) {
            USERNAME = userPassMap.get(getString(R.string.key_username));
            PASSWORD = userPassMap.get(getString(R.string.key_password));
        }

        if (mConfiguration == null) {
            Toast.makeText(this, "还没有进行过定制, 请按提示进行定制", Toast.LENGTH_LONG).show();
            mConfiguration = new WebConfiguration();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewGroup.removeAllViews();
        mWebView.destroy();
    }
}
