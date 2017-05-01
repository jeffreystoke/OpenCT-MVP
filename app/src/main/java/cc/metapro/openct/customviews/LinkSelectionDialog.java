package cc.metapro.openct.customviews;

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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cc.metapro.openct.R;
import cc.metapro.openct.data.source.local.DBManger;
import cc.metapro.openct.data.source.local.LocalHelper;
import cc.metapro.openct.data.university.CmsFactory;
import cc.metapro.openct.utils.ActivityUtils;
import cc.metapro.openct.utils.Constants;
import cc.metapro.openct.utils.base.BaseDialog;
import cc.metapro.openct.utils.base.LoginPresenter;
import cc.metapro.openct.utils.base.MyObserver;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static cc.metapro.openct.utils.Constants.TYPE_BORROW;
import static cc.metapro.openct.utils.Constants.TYPE_CLASS;
import static cc.metapro.openct.utils.Constants.TYPE_GRADE;
import static cc.metapro.openct.utils.Constants.sDetailCustomInfo;


public class LinkSelectionDialog extends BaseDialog {

    private static final String TAG = LinkSelectionDialog.class.getSimpleName();

    private static String TYPE;
    private static boolean sIsFirst;
    private static boolean sShowAll;
    private static Document DOCUMENT;
    private static LoginPresenter mPresenter;
    private Element mTarget;
    private Elements mElements;


    public static LinkSelectionDialog newInstance(String type, Document document,
                                                  LoginPresenter presenter,
                                                  boolean isFirst, boolean showAll) {
        sIsFirst = isFirst;
        sShowAll = showAll;
        TYPE = type;
        DOCUMENT = document;
        mPresenter = presenter;
        return new LinkSelectionDialog();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = ActivityUtils
                .getAlertBuilder(getActivity(), R.string.target_selection)
                .setNeutralButton(R.string.not_in_range_above, null);
        if (!sIsFirst) {
            builder.setNeutralButton(R.string.click_to_go, null);
        }

        setView(builder);
        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                final Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                final Button neutralButton = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL);

                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mTarget == null) {
                            Toast.makeText(getActivity(), "Please select a link first", Toast.LENGTH_LONG).show();
                            return;
                        }

                        setUrlPattern();
                        DBManger.getInstance(getActivity()).updateAdvCustomInfo(sDetailCustomInfo);
                        Constants.checkAdvCustomInfo(getActivity());
                        mPresenter.loadTargetPage(getFragmentManager(), mTarget.absUrl("href"));

                        TYPE = null;
                        mPresenter = null;
                        DOCUMENT = null;
                        dismiss();
                    }
                });

                if (sIsFirst) {
                    neutralButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            newInstance(TYPE, DOCUMENT, mPresenter, false, true)
                                    .show(getFragmentManager(), "link_selection");
                            dismiss();
                        }
                    });
                } else {
                    neutralButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mTarget == null) {
                                Toast.makeText(getActivity(), "Please select a link first", Toast.LENGTH_LONG).show();
                                return;
                            }

                            setUrlPattern();
                            if (TYPE_GRADE.equals(TYPE) || TYPE_CLASS.equals(TYPE)) {
                                Observable<Document> observable = Observable.create(new ObservableOnSubscribe<Document>() {
                                    @Override
                                    public void subscribe(ObservableEmitter<Document> e) throws Exception {
                                        CmsFactory factory = LocalHelper.getCms(getActivity());
                                        e.onNext(factory.getPageDom(mTarget.absUrl("href")));
                                    }
                                });

                                Observer<Document> observer = new MyObserver<Document>(TAG) {
                                    @Override
                                    public void onNext(Document document) {
                                        newInstance(TYPE, document, mPresenter, false, false)
                                                .show(getFragmentManager(), "link_selection");
                                        dismiss();
                                    }
                                };

                                observable.subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(observer);
                            }
                        }
                    });
                }
            }
        });

        return alertDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.checkAdvCustomInfo(getActivity());
    }

    private void setView(AlertDialog.Builder builder) {
        if (!sShowAll) {
            switch (TYPE) {
                case TYPE_CLASS:
                    mElements = DOCUMENT.select("a:matches(课表|课程)");
                    break;
                case TYPE_GRADE:
                    mElements = DOCUMENT.select("a:matches(成绩)");
                    break;
                case TYPE_BORROW:
                    mElements = DOCUMENT.select("a:matches(借阅)");
                    break;
                default:
                    throw new UnsupportedOperationException("not supported operation");
            }
        } else {
            mElements = DOCUMENT.select("a");
        }

        String[] strings = new String[mElements.size()];
        int i = 0;
        for (Element e : mElements) {
            strings[i++] = e.text();
        }

        builder.setSingleChoiceItems(strings, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mTarget = mElements.get(which);
            }
        });
    }

    private void setUrlPattern() {
        if (TYPE_CLASS.equals(TYPE)) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstClassUrlPattern(mTarget.toString());
            } else {
                sDetailCustomInfo.addClassUrlPattern(mTarget.toString());
            }
        } else if (TYPE_GRADE.equals(TYPE)) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstGradeUrlPattern(mTarget.toString());
            } else {
                sDetailCustomInfo.addGradeUrlPattern(mTarget.toString());
            }
        } else if (TYPE_BORROW.equals(TYPE)) {
            if (sIsFirst) {
                sDetailCustomInfo.setFirstBorrowPattern(mTarget.toString());
            } else {
                sDetailCustomInfo.addBorrowPattern(mTarget.toString());
            }
        }
    }

}
