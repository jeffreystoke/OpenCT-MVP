package cc.metapro.openct.grades;

/*
 *  Copyright 2015 2017 metapro.cc Jeffctor
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cc.metapro.openct.R;
import cc.metapro.openct.data.openctservice.ServiceGenerator;
import cc.metapro.openct.data.source.DBManger;
import cc.metapro.openct.data.source.Loader;
import cc.metapro.openct.data.university.UniversityService;
import cc.metapro.openct.data.university.item.GradeInfo;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

class GradePresenter implements GradeContract.Presenter {

    private Context mContext;
    private GradeContract.View mGradeFragment;
    private List<GradeInfo> mGrades;

    GradePresenter(GradeContract.View view, Context context) {
        mContext = context;
        mGradeFragment = view;
        mGradeFragment.setPresenter(this);
    }

    @Override
    public void loadOnline(final String code) {
        ActivityUtils.getProgressDialog(mContext, R.string.loading_grade_infos).show();
        Observable
                .create(new ObservableOnSubscribe<List<GradeInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                        Map<String, String> loginMap = Loader.getCmsStuInfo(mContext);
                        loginMap.put(Constants.CAPTCHA_KEY, code);
                        e.onNext(Loader.getCms().getGrades(loginMap));
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<GradeInfo>>() {
                    @Override
                    public void accept(List<GradeInfo> infos) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        if (infos.size() == 0) {
                            Toast.makeText(mContext, R.string.no_grades_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            mGrades = infos;
                            storeGrades();
                            loadLocalGrades();
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<GradeInfo>>() {
                    @Override
                    public List<GradeInfo> apply(Throwable throwable) throws Exception {
                        ActivityUtils.dismissProgressDialog();
                        Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                })
                .subscribe();

    }

    @Override
    public void loadLocalGrades() {
        Observable
                .create(new ObservableOnSubscribe<List<GradeInfo>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<GradeInfo>> e) throws Exception {
                        DBManger manger = DBManger.getInstance(mContext);
                        e.onNext(manger.getGradeInfos());
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<List<GradeInfo>>() {
                    @Override
                    public void accept(List<GradeInfo> gradeInfos) throws Exception {
                        if (gradeInfos.size() == 0) {
                            Toast.makeText(mContext, R.string.no_local_grades_avail, Toast.LENGTH_SHORT).show();
                        } else {
                            mGrades = gradeInfos;
                            mGradeFragment.onLoadGrades(mGrades);
                        }
                    }
                })
                .onErrorReturn(new Function<Throwable, List<GradeInfo>>() {
                    @Override
                    public List<GradeInfo> apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, mContext.getString(R.string.somthing_wrong) + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return new ArrayList<>(0);
                    }
                }).subscribe();
    }

    @Override
    public void loadCETGrade(final Map<String, String> queryMap) {
        Observable
                .create(new ObservableOnSubscribe<Map<String, String>>() {
                    @Override
                    public void subscribe(ObservableEmitter<Map<String, String>> e) throws Exception {
                        UniversityService service = ServiceGenerator
                                .createService(UniversityService.class, ServiceGenerator.HTML);

                        String res = service.queryCET("http://www.chsi.com.cn/cet/",
                                queryMap.get(Constants.CET_NUM_KEY),
                                queryMap.get(Constants.CET_NAME_KEY), "t")
                                .execute().body();

                        Document document = Jsoup.parse(res);
                        Elements elements = document.select("table[class=cetTable]");
                        Element targetTable = elements.first();
                        Elements tds = targetTable.getElementsByTag("td");
                        String name = tds.get(0).text();
                        String school = tds.get(1).text();
                        String type = tds.get(2).text();
                        String num = tds.get(3).text();
                        String time = tds.get(4).text();
                        String grade = tds.get(5).text();

                        Map<String, String> results = new HashMap<>(6);
                        results.put(Constants.CET_NAME_KEY, name);
                        results.put(Constants.CET_SCHOOL_KEY, school);
                        results.put(Constants.CET_TYPE_KEY, type);
                        results.put(Constants.CET_NUM_KEY, num);
                        results.put(Constants.CET_TIME_KEY, time);
                        results.put(Constants.CET_GRADE_KEY, grade);

                        e.onNext(results);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Map<String, String>>() {
                    @Override
                    public void accept(Map<String, String> stringMap) throws Exception {
                        mGradeFragment.onLoadCETGrade(stringMap);
                    }
                })
                .onErrorReturn(new Function<Throwable, Map<String, String>>() {
                    @Override
                    public Map<String, String> apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, R.string.load_cet_grade_fail, Toast.LENGTH_SHORT).show();
                        return new HashMap<>();
                    }
                })
                .subscribe();
    }

    @Override
    public void loadCaptcha(final TextView view) {
        Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter e) throws Exception {
                        Loader.getCms().getCAPTCHA();
                        e.onComplete();
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorReturn(new Function<Throwable, String>() {
                    @Override
                    public String apply(Throwable throwable) throws Exception {
                        Toast.makeText(mContext, "获取验证码失败\n" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        return "";
                    }
                })
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        Drawable drawable = BitmapDrawable.createFromPath(Constants.CAPTCHA_FILE);
                        if (drawable != null) {
                            view.setBackground(drawable);
                            view.setText("");
                        }
                    }
                })
                .subscribe();
    }

    @Override
    public void storeGrades() {
        try {
            DBManger manger = DBManger.getInstance(mContext);
            manger.updateGradeInfos(mGrades);
        } catch (Exception e) {
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void clearGrades() {
        mGrades = new ArrayList<>(0);
        storeGrades();
    }

    @Override
    public void start() {
        loadLocalGrades();
    }
}
